# Wallet Service — API

- **Base URL (direct):** `http://localhost:8084`
- **Via gateway:** `http://localhost:8080/wallets/**`
- **Role:** owns Paytm/PhonePe wallets. Calls Customer (validate), Transaction (record), Notification (limit-exceeded).

## Endpoints exposed

### `POST /wallets` — create a wallet
Request:
```json
{ "customerId": "6699a1f2c3d4e5f601234567", "provider": "PAYTM", "maxLimit": 10000.00 }
```
Response `201`: wallet document. Error: `404` unknown customer.
→ **Calls** `GET /customers/{customerId}` first.

### `GET /wallets?customerId={id}` — list a customer's wallets
Response `200`: array of wallets.

### `POST /wallets/{id}/add` — add money (Add Money)
Ported from `WalletOps` → Add Money.
Request: `{ "amount": 500.00 }` → Response `200`: updated wallet.
→ **Calls** `POST /transactions` (`direction=WALLET_ADD`).
On Paytm cap exceeded → **Calls** `POST /notifications` and returns `400`.

### `POST /wallets/{id}/pay` — pay a bill (Pay Bill)
Ported from `WalletOps` → Pay Bill.
Request: `{ "amount": 200.00 }` → Response `200`: updated wallet.
→ **Calls** `POST /transactions` (`direction=WALLET_PAY`). Error: `400` insufficient balance.

### `POST /wallets/{id}/transfer` — transfer to another wallet (Transfer to Wallet)
Ported from `WalletOps` → Transfer to Wallet.
Request: `{ "targetWalletId": "6699a7...73", "amount": 300.00 }` → Response `200`: both wallets.
→ **Calls** `POST /transactions` twice. Error: `400` insufficient balance.

## Endpoints called (outbound)

| Target service | URL | When |
|----------------|-----|------|
| Customer | `GET /customers/{id}` | Validate owner on wallet creation. |
| Transaction | `POST /transactions` | Record every add / pay / transfer leg. |
| Notification | `POST /notifications` | Paytm balance exceeds `maxLimit` (replaces `FileLogger`). |

## Error mapping

| Exception / condition | HTTP status |
|-----------------------|-------------|
| `InsufficientBalanceException` | 400 Bad Request |
| Paytm limit exceeded | 400 Bad Request (+ notification logged) |
| `InvalidAmountException` | 422 Unprocessable Entity |
| unknown customer / wallet | 404 Not Found |
