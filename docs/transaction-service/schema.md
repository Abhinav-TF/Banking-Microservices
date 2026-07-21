# Transaction Service — MongoDB Schema

- **Database:** `transactiondb`
- **Collection:** `transactions`
- **Port:** 8083
- **Ported from:** `model/Transaction.java`, `util/TransactionList.java`, `service/ViewTransactions.java`

## Document fields

| Field | BSON type | Constraints | Notes |
|-------|-----------|-------------|-------|
| `_id` | ObjectId | primary key, auto-generated | Replaces the old static `Transaction.counter`. |
| `ownerId` | string | required | Customer id the transaction belongs to. Was tracked via `Customer.transactionsOfCustomer`. |
| `type` | string (enum) | `SAVINGS` \| `CURRENT` \| `PAYTM` \| `PHONEPE` | Source instrument type. |
| `direction` | string (enum) | `DEPOSIT` \| `WITHDRAW` \| `TRANSFER_IN` \| `TRANSFER_OUT` \| `WALLET_ADD` \| `WALLET_PAY` | New — distinguishes movement kind. |
| `amount` | decimal (`Decimal128`) | required, > 0 | Was `final double amount`; use Decimal128 to avoid float rounding. |
| `balanceAfter` | decimal (`Decimal128`) | required | Post-transaction balance. Was `double balance`. |
| `timestamp` | date | set on insert | Was `LocalDateTime timestamp`. |

## Indexes

| Index | Fields | Type | Reason |
|-------|--------|------|--------|
| `_id_` | `_id` | default | Primary key. |
| `ix_owner_ts` | `ownerId` asc, `timestamp` desc | compound | Fast per-customer statement, newest first. |

## Example document

```json
{
  "_id": { "$oid": "6699a2aabbccddee01234568" },
  "ownerId": "6699a1f2c3d4e5f601234567",
  "type": "SAVINGS",
  "direction": "DEPOSIT",
  "amount": { "$numberDecimal": "5000.00" },
  "balanceAfter": { "$numberDecimal": "15000.00" },
  "timestamp": { "$date": "2026-07-16T10:20:00Z" }
}
```

## Notes

- Replaces the global static `util/TransactionList` `HashMap<Integer, Transaction>` with a
  persisted collection and DB-generated ids.
- This service **only stores and returns** transaction records. Balance calculation happens in the
  Account/Wallet services, which pass the resulting `balanceAfter` when recording.
