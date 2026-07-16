# Customer Service — MongoDB Schema

- **Database:** `customerdb`
- **Collection:** `customers`
- **Port:** 8081
- **Ported from:** `model/Customer.java`, `model/CustomerService.java`

## Document fields

| Field | BSON type | Constraints | Notes |
|-------|-----------|-------------|-------|
| `_id` | ObjectId | primary key, auto-generated | Replaces the old static `customerCounter`. |
| `name` | string | required, non-blank | Was `customerName`. |
| `email` | string | required, unique, must contain `@` | `validateEmail` → `InvalidEmailException`. |
| `phone` | string | required, regex `^[0-9]{10}$` | `validateMobileNo` → `InvalidPhoneNumberException`. |
| `createdAt` | date | set on insert | New field for audit. |

## Indexes

| Index | Fields | Type | Reason |
|-------|--------|------|--------|
| `_id_` | `_id` | default | Primary key. |
| `uk_email` | `email` | unique | Enforces `DuplicateCustomerException` at the DB level. |

## Validation rules (ported from `CustomerService`)

- **Unique customer** — duplicate email/id rejected → `DuplicateCustomerException` (HTTP 409).
- **Email** — must contain `@` → `InvalidEmailException` (HTTP 400).
- **Phone** — exactly 10 digits `[0-9]{10}` → `InvalidPhoneNumberException` (HTTP 400).

## Example document

```json
{
  "_id": { "$oid": "6699a1f2c3d4e5f601234567" },
  "name": "Abhinav Singh",
  "email": "abhinav@example.com",
  "phone": "9876543210",
  "createdAt": { "$date": "2026-07-16T10:15:30Z" }
}
```

## Notes

- The legacy `Customer` also stored `ArrayList<Integer> transactionsOfCustomer` and
  `HashMap<String, PaymentType> bankAccount`. **These are removed** — transactions live in the
  Transaction Service and accounts/wallets in their own services, referenced by `customerId`.
- The `Customer extends CustomerService` inheritance is dropped; the registry logic becomes the
  service layer over a `CustomerRepository extends MongoRepository<Customer, String>`.
