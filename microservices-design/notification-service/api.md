# Transaction Service — API

- **Base URL (direct):** `http://localhost:8083`
- **Via gateway:** `http://localhost:8080/transactions/**`
- **Role:** leaf service — the transaction ledger. Written to by Account & Wallet; **calls no other service**.

## Authentication

- **`GET /logs?ownerId=` (client reads)** go through the gateway and carry a verified `X-Customer-Id`;
  the list is scoped to that owner (`403 Forbidden` if `ownerId` is another customer).
- **`POST /notifications` (writes)** are internal, from other services — not a client-facing route.
  (In the Kafka variant this write endpoint is removed entirely; see `kafka-design.md`.)

## Endpoints exposed

### `POST /transactions` — record a transaction
Called by Account Service and Wallet Service after every successful money movement.

Request:
```json
{
  "ownerId": "6699a1f2c3d4e5f601234567",
  "type": "SAVINGS",
  "direction": "DEPOSIT",
  "amount": 5000.00,
  "balanceAfter": 15000.00
}
```
Response `201 Created`:
```json
{
  "id": "6699a2aabbccddee01234568",
  "ownerId": "6699a1f2c3d4e5f601234567",
  "type": "SAVINGS",
  "direction": "DEPOSIT",
  "amount": 5000.00,
  "balanceAfter": 15000.00,
  "timestamp": "2026-07-16T10:20:00Z"
}
```

### `GET /transactions/{id}` — fetch one transaction
Ported from `TransactionList.getTransaction(Integer id)`.
Response `200`: single transaction. `404` if not found.

### `GET /transactions?ownerId={id}` — list a customer's transactions
Ported from `Customer.getAllTransactions()` / `service/ViewTransactions`.
Response `200`: array of transactions for `ownerId`, newest first.

## Endpoints called (outbound)

_None._ Transaction Service is a leaf.
