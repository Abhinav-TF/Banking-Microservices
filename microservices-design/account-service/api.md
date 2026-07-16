# Account Service — API

- **Base URL (direct):** `http://localhost:8082`
- **Via gateway:** `http://localhost:8080/accounts/**`
- **Role:** owns bank accounts + balance operations. Calls Customer (validate) and Transaction (record).

## Authentication

- Behind the gateway JWT filter; every request carries a verified `X-Customer-Id` header.
- **`customerId` is taken from `X-Customer-Id`, not the request body** — a caller can only open and
  operate on accounts for themselves. The `customerId` field is dropped from `POST /accounts`.
- Balance operations and `GET /accounts?customerId=` are ownership-checked against `X-Customer-Id`
  (`403 Forbidden` if the account/list belongs to another customer).

## Endpoints exposed

### `POST /accounts` — open an account
Ported from `service/CreateAccount`. `customerId` comes from the authenticated `X-Customer-Id`.
Request:
```json
{ "type": "SAVINGS", "accountNumber": 100001, "openingBalance": 10000.00 }
```
Response `201`: the account document. Errors: `404` unknown customer · `409` duplicate accountNumber.
→ **Calls** `GET /customers/{customerId}` first.

### `GET /accounts/{id}` — fetch one account
Response `200`: account document. `404` if not found.

### `GET /accounts?customerId={id}` — list a customer's accounts
Response `200`: array of accounts.

### `POST /accounts/{id}/deposit` — deposit money
Ported from `service/DepositMoney`.
Request: `{ "amount": 5000.00 }` → Response `200`: updated account.
→ **Calls** `POST /transactions` (`direction=DEPOSIT`). Error: `422` non-positive amount.

### `POST /accounts/{id}/withdraw` — withdraw money
Ported from `service/WithdrawMoney`.
Request: `{ "amount": 2000.00 }` → Response `200`: updated account.
→ **Calls** `POST /transactions` (`direction=WITHDRAW`). Errors: `400` below min balance · `422` bad amount.

### `POST /accounts/transfer` — transfer between accounts
Ported from `service/TransferMoney`. Orchestrated here: withdraw source → deposit target → record two transactions.
Request:
```json
{ "sourceAccountId": "6699a4...70", "targetAccountId": "6699a5...71", "amount": 3000.00 }
```
Response `200`: both updated account balances.
→ **Calls** `POST /transactions` twice (`TRANSFER_OUT` on source, `TRANSFER_IN` on target).
Errors: `400` insufficient funds · `404` unknown account.

## Endpoints called (outbound)

| Target service | URL | When |
|----------------|-----|------|
| Customer | `GET /customers/{id}` | Validate owner on account creation. |
| Transaction | `POST /transactions` | Record every deposit / withdraw / transfer leg. |

## Error mapping

| Exception | HTTP status |
|-----------|-------------|
| `InsufficientBalanceException` | 400 Bad Request |
| `InvalidAmountException` | 422 Unprocessable Entity |
| unknown customer / account | 404 Not Found |
| duplicate accountNumber | 409 Conflict |
| account/list owned by another customer | 403 Forbidden |
