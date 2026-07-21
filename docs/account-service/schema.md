# Account Service — MongoDB Schema

- **Database:** `accountdb`
- **Collection:** `accounts`
- **Port:** 8082
- **Ported from:** `model/BankAccount.java`, `SavingsAccount.java`, `CurrentAccount.java`

## Document fields

| Field | BSON type | Constraints | Notes |
|-------|-----------|-------------|-------|
| `_id` | ObjectId | primary key, auto-generated | Internal id. |
| `accountNumber` | int (`NumberLong`) | required, unique | Human-friendly business number (was `int accountNumber`). |
| `customerId` | string | required, ref → `customerdb.customers._id` | Owner. Validated via Customer Service. |
| `type` | string (enum) | `SAVINGS` \| `CURRENT` | Was the `SavingsAccount`/`CurrentAccount` subclasses. |
| `balance` | decimal (`Decimal128`) | required, ≥ 0 | Was `double balance`. |
| `createdAt` | date | set on insert | |

## Indexes

| Index | Fields | Type | Reason |
|-------|--------|------|--------|
| `_id_` | `_id` | default | Primary key. |
| `uk_accountNumber` | `accountNumber` | unique | No two accounts share a number. |
| `ix_customer` | `customerId` | single | List all accounts for a customer. |

## Business rules (ported from subclasses)

| Type | Rule | On violation |
|------|------|--------------|
| `SAVINGS` | balance may not drop below `MIN_BALANCE = 0` on withdraw | `InsufficientBalanceException` (400) |
| `CURRENT` | balance may not go negative | `InsufficientBalanceException` (400) |
| both | deposit/withdraw amount must be > 0 | `InvalidAmountException` (422) |

`type` replaces class inheritance: a single `Account` document with a discriminator, rules applied
in the service layer.

## Example document

```json
{
  "_id": { "$oid": "6699a4ccddeeff0001234570" },
  "accountNumber": 100001,
  "customerId": "6699a1f2c3d4e5f601234567",
  "type": "SAVINGS",
  "balance": { "$numberDecimal": "15000.00" },
  "createdAt": { "$date": "2026-07-16T10:18:00Z" }
}
```

## Notes

- No `Customer` object is embedded — only `customerId`. Existence is verified via a REST call to
  Customer Service at account-creation time.
- Every deposit/withdraw/transfer records to Transaction Service with the resulting `balanceAfter`.
