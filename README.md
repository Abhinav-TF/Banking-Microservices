# T&F International Bank — Microservices

A Spring Boot + Spring Cloud implementation of a banking platform, decomposed from a single-process
console app into independently deployable microservices. Services discover each other through
**Eureka**, are fronted by a **Spring Cloud Gateway**, authenticate via **JWT**, and persist to
**MongoDB** (one database per service).

> ⚠️ **Demo / learning project.** Passwords are stored in **plain text** by design for this
> iteration — never use this as-is in production.

---

## Architecture

```
                       ┌──────────────┐
   client ──JWT──────► │  API Gateway │ :8080   (validates JWT, injects X-Customer-Id)
                       └──────┬───────┘
             ┌────────────────┼───────────────┬───────────────┬──────────────┐
             ▼                ▼                ▼               ▼              ▼
        ┌─────────┐     ┌──────────┐    ┌────────────┐  ┌──────────┐  ┌──────────┐
        │  Auth   │     │ Customer │    │  Account   │  │  Wallet  │  │Transaction│
        │  :8086  │     │  :8081   │    │   :9001    │  │  :8084   │  │  :8083   │
        └────┬────┘     └────▲─────┘    └──┬─────┬───┘  └─┬────┬───┘  └────▲─────┘
             │ register      │ validate    │     │ record │    │ record    │
             └───────────────┘             │     └────────┼────┼───────────┘
                                           └──────────────┘    │ validate
                                                               ▼
                                                          Customer

        All services register with ► Eureka (:8761)
```

- **Communication:** synchronous REST via **OpenFeign**, resolved by service name through Eureka.
- **Leaf services** (no outbound calls): Customer, Transaction.
- **Auth** → Customer. **Account** → Customer + Transaction. **Wallet** → Customer + Transaction.

### Service map

| Service | Port | Mongo DB | Role | Status |
|---|---|---|---|---|
| eureka-server | 8761 | — | Service registry | ✅ implemented |
| api-gateway | 8080 | — | Single entry point, JWT validation, routing | ✅ implemented |
| auth-service | 8086 | `authdb` | Register/login, issues HS256 JWT | ✅ implemented |
| customer-service | 8081 | `customerdb` | Customer CRUD + `/exists` validation | ✅ implemented |
| transaction-service | 8083 | `transactiondb` | Transaction ledger (leaf) | ✅ implemented |
| account-service | 9001 | `accountdb` | Accounts, deposit/withdraw/transfer | ✅ implemented |
| wallet-service | 8084 | `walletdb` | Wallets, add/pay/transfer | ✅ implemented |

> The end-to-end workflow **register → login → authenticated reads → record & read transactions**
> (gateway + auth + customer + transaction) is covered by the live smoke tests below.

---

## Tech stack

- **Java 25** (Oracle JDK 26 tested), **Maven**
- **Spring Boot** 3.5.x / 4.1.x, **Spring Cloud** 2025.x
- **Spring Cloud Gateway**, **Netflix Eureka**, **OpenFeign**
- **MongoDB** (Spring Data MongoDB), **JJWT** (HS256)
- Observability hooks: Actuator, Micrometer/Prometheus, Zipkin (B3) tracing

---

## Prerequisites

- JDK 25+ and Maven 3.9+
- A running **MongoDB** on `localhost:27017`
- Ports free: `8080, 8081, 8083, 8084, 8086, 9001, 8761`

---

## Build & run

Each service is an independent Maven module — build and run them individually.

```bash
# Build (per service)
cd <service> && mvn -DskipTests package

# Run (start Eureka first, then the rest)
java -jar eureka-server/target/eureka-server-*.jar
java -jar api-gateway/target/api-gateway-*.jar
java -jar auth-service/target/auth-service-*.jar
java -jar customer-service/target/customer-service-*.jar
java -jar transaction-service/target/transaction-service-*.jar
java -jar account-service/target/account-service-*.jar
java -jar wallet-service/target/wallet-service-*.jar
```

MongoDB URIs can be overridden with the `MONGODB_URI` env var; Eureka with `EUREKA_URI` / `EUREKA_URL`.
The JWT signing secret (`JWT_SECRET`) **must match** between api-gateway and auth-service.

Verify registration once everything is up:

```bash
curl -s http://localhost:8761/eureka/apps -H 'Accept: application/json'   # registered instances
curl -s http://localhost:8080/actuator/health                            # gateway health
```

---

## Authentication & security

- `POST /auth/register` and `POST /auth/login` are **public**; every other route requires a JWT.
- Auth issues an **HS256** JWT (`sub = customerId`, `email` claim, 1h expiry). The **gateway**
  validates it, **strips any client-supplied identity headers**, and injects trusted
  `X-Customer-Id` / `X-Auth-Email` for downstream services.
- Downstream services trust the gateway headers — **not** the client. Ownership is enforced from
  `X-Customer-Id`:
  - `GET /customers/{id}` → `401` if header absent, `403` if it isn't the caller's own id.
  - `GET /transactions` / `GET /transactions/{id}` → `401` if absent, `403` if the owner isn't the caller.
- `GET /customers/{id}/exists` is the **internal** owner-validation endpoint (service-to-service via
  Eureka, no JWT) — not ownership-checked.

---

## API reference

All client calls go through the gateway (`http://localhost:8080`) with `Authorization: Bearer <jwt>`
(except the public auth routes). Ports shown are for direct/internal access.

### Auth — `:8086`
| Method | Path | Notes |
|---|---|---|
| POST | `/auth/register` | Public. Creates the customer (Feign → customer-service) + credential. |
| POST | `/auth/login` | Public. Returns JWT. |
| GET | `/auth/me` | Current identity from token. |

### Customer — `:8081`
| Method | Path | Notes |
|---|---|---|
| POST | `/customers` | Create (normally called by Auth). |
| GET | `/customers/{id}` | Ownership-checked. |
| GET | `/customers/{id}/exists` | Internal validation, not ownership-checked. |
| GET | `/customers` | List (`?excludeId=` supported). |

### Transaction — `:8083`
| Method | Path | Notes |
|---|---|---|
| POST | `/transactions` | Internal write (called by Account/Wallet). Payload-validated. |
| GET | `/transactions/{id}` | Ownership-checked. |
| GET | `/transactions?ownerId=` | Ownership-checked; scoped to the caller. |

### Account — `:9001`
| Method | Path |
|---|---|
| POST | `/accounts` |
| GET | `/accounts/{id}` |
| GET | `/accounts` |
| POST | `/accounts/{id}/deposit` |
| POST | `/accounts/{id}/withdraw` |
| POST | `/accounts/transfer` |

### Wallet — `:8084`
| Method | Path |
|---|---|
| POST | `/wallets` |
| GET | `/wallets` |
| POST | `/wallets/{id}/add` |
| POST | `/wallets/{id}/pay` |
| POST | `/wallets/{id}/transfer` |

---

## Example workflow

```bash
GW=http://localhost:8080

# 1. Register (gateway → auth → Feign → customer)
curl -s -X POST $GW/auth/register -H 'Content-Type: application/json' \
  -d '{"name":"Alice","email":"alice@example.com","phone":"9876543210","password":"secret1"}'

# 2. Login → capture JWT
TOKEN=$(curl -s -X POST $GW/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"secret1"}' | jq -r .token)

# 3. Authenticated read of own customer record
curl -s $GW/customers/<customerId> -H "Authorization: Bearer $TOKEN"

# 4. Read own transactions (scoped to the authenticated caller)
curl -s "$GW/transactions?ownerId=<customerId>" -H "Authorization: Bearer $TOKEN"
```

---

## Testing

Verified live against the running stack (register → login → authenticated reads → record/read
transactions, plus auth/gateway/ownership negative cases): **21/21 checks pass**. Highlights:

- Cross-service registration (auth → customer via Feign): `201`
- Gateway JWT enforcement: missing/garbage token → `401`
- Ownership isolation: accessing another customer's record or transactions → `403`
- Input validation on `POST /transactions`: bad enum / negative amount → `400`
- Persistence lands in the correct per-service databases.

Unit/integration tests per module run with `mvn test`.

---

## Centralised logging (ELK)

All services ship structured **JSON logs** to a central **ELK stack** (Elasticsearch + Logstash +
Kibana) so logs from every microservice are searchable in one place, correlated by trace id.

**Pipeline:** each service's Logback `LogstashTcpSocketAppender` → **Logstash** (`:50000`, TCP,
`json_lines`) → **Elasticsearch** (`banking-logs-*` daily indices) → **Kibana**.

- Every service logs to console, a rolling file (`logs/<service>.log`), **and** Logstash
  (`src/main/resources/logback-spring.xml`). Each event is tagged with a `service` field and carries
  `traceId`/`spanId` from Micrometer tracing.
- The appender is **non-blocking and auto-reconnecting** — if the ELK stack is down, services run
  normally and buffer/drop without impact.

### Run the ELK stack

```bash
docker compose -f elk/docker-compose.yml up -d
```

| Component | URL |
|---|---|
| Elasticsearch | http://localhost:9200 |
| Kibana | http://localhost:5601 |
| Logstash TCP input | `localhost:50000` |

Then (re)start the microservices so they connect to Logstash. Override the target with
`LOGSTASH_DESTINATION=host:port` if Logstash isn't on `localhost:50000`.

### View logs in Kibana

1. Open http://localhost:5601 → **Stack Management → Data Views → Create data view**.
2. Index pattern: `banking-logs-*`, time field: `@timestamp`.
3. Explore in **Discover**; filter by `service` (e.g. `service: "auth-service"`) or `traceId`.

Quick check that logs are landing:

```bash
curl -s "http://localhost:9200/banking-logs-*/_count"
curl -s "http://localhost:9200/banking-logs-*/_search?q=service:auth-service&size=1&pretty"
```

Stop the stack with `docker compose -f elk/docker-compose.yml down` (add `-v` to drop indices).

---

## Project layout

```
Banking-Microservices/
├── eureka-server/         # service registry
├── api-gateway/           # Spring Cloud Gateway + JWT filter
├── auth-service/          # register/login, JWT issuance, Feign → customer
├── customer-service/      # customer CRUD + /exists
├── transaction-service/   # transaction ledger
├── account-service/       # accounts + money movement
├── wallet-service/        # wallets
├── elk/                   # ELK stack: docker-compose + Logstash pipeline/config
└── microservices-design/  # design docs (per-service api.md / schema.md, infra)
```
