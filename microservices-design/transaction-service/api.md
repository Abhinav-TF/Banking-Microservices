# Transaction Service — API

- **Base URL (direct):** `http://localhost:8083`
- **Via gateway:** `http://localhost:8080/transactions/**`
- **Role:** leaf service — the transaction ledger. Written to by Account & Wallet; **calls no other service**.

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
