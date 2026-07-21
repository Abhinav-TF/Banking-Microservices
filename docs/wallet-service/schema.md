# Wallet Service — MongoDB Schema

- **Database:** `walletdb`
- **Collection:** `wallets`
- **Port:** 8084
- **Ported from:** `wallet/WalletOperations`, `wallet/PaytmWallet`, `wallet/PhonePeWallet`

## Document fields

| Field | BSON type | Constraints | Notes |
|-------|-----------|-------------|-------|
| `_id` | ObjectId | primary key, auto-generated | |
| `customerId` | string | required, ref → `customerdb.customers._id` | Owner. Validated via Customer Service. |
| `provider` | string (enum) | `PAYTM` \| `PHONEPE` | Was the two wallet subclasses. |
| `balance` | decimal (`Decimal128`) | required, ≥ 0 | Current wallet balance. |
| `maxLimit` | decimal (`Decimal128`) | required for `PAYTM` | Paytm cap that triggers a notification when exceeded. |
| `createdAt` | date | set on insert | |

## Indexes

| Index | Fields | Type | Reason |
|-------|--------|------|--------|
| `_id_` | `_id` | default | Primary key. |
| `ix_customer_provider` | `customerId` asc, `provider` asc | compound | One lookup for a customer's wallet of a given provider. |

## Business rules

| Provider | Rule | Effect |
|----------|------|--------|
| `PAYTM` | balance may not exceed `maxLimit` on add | Reject + **POST a log** to Notification Service (`"Wallet has exceeded the maximum amount"`). |
| both | pay/transfer may not overdraw balance | `InsufficientBalanceException` (400). |
| both | amount must be > 0 | `InvalidAmountException` (422). |

## Example document

```json
{
  "_id": { "$oid": "6699a6ddeeff000101234572" },
  "customerId": "6699a1f2c3d4e5f601234567",
  "provider": "PAYTM",
  "balance": { "$numberDecimal": "800.00" },
  "maxLimit": { "$numberDecimal": "10000.00" },
  "createdAt": { "$date": "2026-07-16T10:30:00Z" }
}
```

## Notes

- The legacy `PaymentType` marker interface (which let wallets and accounts share a map on
  `Customer`) is gone — wallets are a fully separate service.
- Wallet movements are recorded in Transaction Service with `type=PAYTM|PHONEPE`.
