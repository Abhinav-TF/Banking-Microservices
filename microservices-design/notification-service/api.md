# Notification / Logging Service — API

- **Base URL (direct):** `http://localhost:8085`
- **Via gateway:** `http://localhost:8080/notifications/**` and `http://localhost:8080/logs/**`
- **Role:** leaf service — central event/error log. Written to by other services; **calls no other service**.

## Authentication

- **`GET /logs?ownerId=` (client reads)** go through the gateway and carry a verified `X-Customer-Id`;
  the list is scoped to that owner (`403 Forbidden` if `ownerId` is another customer).
- **`POST /notifications` (writes)** are internal, from other services — not a client-facing route.
  (In the Kafka variant this write endpoint is removed entirely; see `kafka-design.md`.)

## Endpoints exposed

### `POST /notifications` — record an event / log entry
Called by Wallet Service on Paytm limit-exceeded (replaces the direct `FileLogger` write). Open to any service.

Request:
```json
{
  "ownerId": "6699a1f2c3d4e5f601234567",
  "level": "ERROR",
  "source": "wallet-service",
  "message": "Wallet has exceeded the maximum amount"
}
```
Response `201 Created`: the persisted log document (with `id` and `timestamp`).

### `GET /logs?ownerId={id}` — list a customer's events
Replaces reading the per-customer `{customerId}.txt` file.
Response `200`: array of log documents for `ownerId`, newest first.

## Endpoints called (outbound)

_None._ Notification Service is a leaf.
