# Notification / Logging Service — Responsibilities & Event Flow (Kafka)

This document describes **what the Notification Service does, how much it does, and how the other
services talk to it** once the write path is moved from synchronous REST to **Apache Kafka**.

It complements the existing `api.md` (endpoints) and `schema.md` (MongoDB document) in this folder.

---

## 1. Purpose — what it is

The Notification/Logging Service is the bank's **central event sink**. Every other service emits
events ("a wallet exceeded its limit", "an account was created", etc.); this service **records them**
in one place (`notificationdb.logs`) so they can be read back per customer.

It is the successor to the legacy `util/FileLogger.java`, which appended `"[ERROR] " + message` to a
per-customer `.txt` file. That file-based, single-purpose logger becomes a proper, queryable,
multi-service event log.

- **Port:** 8085 · **Eureka name:** `notification-service` · **Database:** `notificationdb`, collection `logs`
- **Role in the graph:** a **leaf** — it never calls another service.

---

## 2. Scope — how much it does (and does NOT do)

### It DOES
- **Consume** event messages from a Kafka topic and **persist** each as a `logs` document
  (`ownerId`, `level`, `source`, `message`, server-set `timestamp`).
- **Expose a read API** so events can be listed for a customer: `GET /logs?ownerId={id}` (newest first).

### It does NOT
- **No write REST endpoint.** The old `POST /notifications` is **removed** — all writes now arrive
  over Kafka. (`GET /logs` remains, for reads only.)
- **No outbound calls.** It never calls Customer, Wallet, Account, or Transaction.
- **No routing / orchestration.** It does not forward messages or coordinate other services (see §5).
- **No business rules.** It does not decide *when* an event matters — producers decide that and emit.
- **No delivery guarantees beyond "record what arrives".** Error handling is intentionally **simple**
  (Spring Boot default listener error handling; no dead-letter topic in this iteration).

### Design decisions locked in
| Concern | Decision |
|---------|----------|
| Write path | **Replace** `POST /notifications` with Kafka entirely |
| Serialization | **JSON** via spring-kafka `JsonSerializer` / `JsonDeserializer` |
| Error handling | **Simple** — Boot defaults, no DLT |
| Base package | `com.tnf.notification` (mirrors `com.tnf.customer`) |

---

## 3. The event contract

The contract is the shared agreement between every producer and this consumer.

- **Topic:** `notification-events`
- **Consumer group:** `notification-service`
- **Message value (JSON):**

```json
{
  "ownerId": "6699a1f2c3d4e5f601234567",
  "level": "WARN",
  "source": "wallet-service",
  "message": "Wallet has exceeded the maximum amount"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `ownerId` | string | optional | Customer the event relates to (was the `.txt` filename). Indexed for `GET /logs`. |
| `level` | enum `INFO\|WARN\|ERROR` | yes | Replaces the hardcoded `"[ERROR]"` prefix. |
| `source` | string | yes | Originating service, e.g. `wallet-service`. |
| `message` | string | yes | Human-readable text. |

The consumer adds `_id` and `timestamp` on insert.

**Cross-service JSON rule:** producers live in different packages, so the consumer must **not** rely
on Kafka `__TypeId__` type headers. It is configured with
`spring.json.use.type.headers=false`, `spring.json.value.default.type=com.tnf.notification.event.NotificationEvent`,
and `spring.json.trusted.packages=*`. This means **any producer can send a plain JSON object with the
field names above** — no shared class or shared library is required.

---

## 4. How other services talk to it (producer → consumer)

Communication is **asynchronous and one-way**. A producer does not wait for, or get a response from,
the Notification Service.

```
Producer service                     Kafka                    Notification Service
----------------                  -----------                 --------------------
build NotificationEvent  ──publish──▶  topic:      ──consume──▶  @KafkaListener
(ownerId, level,                    notification-                 └─ persist to
 source, message)                    events                          notificationdb.logs
      │
      └─ continues immediately (fire-and-forget)
```

- **Producers** add `spring-kafka`, a `KafkaTemplate`, and publish a `NotificationEvent` JSON to
  `notification-events`.
- **Current producer:** **Wallet Service** — on `POST /wallets/{id}/add`, when a **PAYTM** wallet
  would exceed `maxLimit`, it publishes an event (`level=WARN`, `source=wallet-service`,
  `message="Wallet has exceeded the maximum amount"`) instead of the old Feign call to
  `POST /notifications`, then returns `400` to its own caller.
- **Future producers:** Account, Transaction, Customer may emit events (e.g. account created,
  transfer completed) by publishing to the same topic with their own `source`. No change is needed in
  the Notification Service to onboard them — that is the point of the shared topic + contract.

### Consuming side (inside this service)
1. `@KafkaListener(topics = "notification-events", groupId = "notification-service")` receives a
   `NotificationEvent`.
2. It maps the event to a `Log` document and saves it via `LogRepository`.
3. That's it — no acknowledgement flows back to the producer.

---

## 5. "Through it to each other" — what this service is and isn't

The Notification Service is a **sink, not a router / message broker**. Services do **not** talk *to
each other through* it. Two distinct things share the word "message":

- **Kafka (the broker)** is the medium services communicate *through* asynchronously. If in future
  two services need to exchange events, they do so via **their own Kafka topics**, not by relaying
  through the Notification Service.
- **The Notification Service** is just **one consumer** on **one topic** (`notification-events`). It
  reads events and stores them. It has no knowledge of, and no ability to reach, any other service.

So the accurate picture of inter-service communication in the system is:

| Interaction | Mechanism | Direction |
|-------------|-----------|-----------|
| Client → Auth (register / login) | **REST via API Gateway** (`:8080`, public paths) | request/response |
| Auth → Customer (create on register) | **Synchronous REST** (Feign via Eureka) | request/response |
| Account/Wallet → Customer (validate owner) | **Synchronous REST** (Feign via Eureka) | request/response |
| Account/Wallet → Transaction (record) | **Synchronous REST** (Feign via Eureka) | request/response |
| Wallet (and others) → Notification (log event) | **Kafka** topic `notification-events` | fire-and-forget |
| Client → any service | **REST via API Gateway** (`:8080`, JWT-validated) | request/response |

Only the **write path into Notification** becomes Kafka. The validate/record calls between the
business services stay synchronous REST (they need an immediate answer). Notification is the one
interaction that is naturally fire-and-forget, which is why it is the one moved to Kafka.

---

## 6. Read API (unchanged, REST)

Reading events stays a normal REST call — this is the only HTTP endpoint the service exposes.

- `GET /logs?ownerId={id}` → `200` array of `logs` documents for that customer, newest first.
- Direct: `http://localhost:8085/logs` · via gateway: `http://localhost:8080/logs/**`.

---

## 7. Worked example — Paytm limit exceeded

```
1. Client → Gateway → Wallet:  POST /wallets/{id}/add  { amount: 5000 }
2. Wallet computes new balance > maxLimit (PAYTM rule).
3. Wallet PUBLISHES to Kafka topic "notification-events":
      { ownerId, level: "WARN", source: "wallet-service",
        message: "Wallet has exceeded the maximum amount" }
4. Wallet returns 400 Bad Request to the client (does NOT wait for step 5).
5. Notification Service's @KafkaListener consumes the event and inserts a "logs" document.
6. Later: Client → GET /logs?ownerId={id} → sees the WARN entry.
```

---

## 8. Infrastructure needed

- **Kafka broker** (single node, KRaft mode — no Zookeeper) reachable at `localhost:9092`, e.g. via a
  `docker-compose.yml` at the repo root. Topic `notification-events` (auto-created, or created once).
- **MongoDB** (`localhost:27017`, database `notificationdb`).
- **Eureka** (`localhost:8761`) — the service still registers so the gateway can route `/logs/**`.

---

## 9. Summary

| Question | Answer |
|----------|--------|
| What does it do? | Consumes event messages from Kafka and stores them; serves them back via `GET /logs`. |
| How much? | Just that — a durable, queryable, multi-service event log. No routing, no outbound calls, no write REST endpoint, simple error handling. |
| How do services talk to it? | They **publish** JSON `NotificationEvent`s to the `notification-events` topic (async, fire-and-forget). Wallet is the first producer. |
| Do services talk *through* it? | No. It is a sink. Services communicate *through Kafka* (their own topics) or *via REST*; Notification is one consumer that only records. |
