# Auth Service — MongoDB Schema

- **Database:** `authdb`
- **Collection:** `credentials`
- **Port:** 8086
- **Role:** platform/security service — stores login credentials and issues JWTs. Calls Customer
  Service on registration. **Not a leaf** (Auth → Customer).

> ⚠️ **Passwords are stored in plain text, by explicit design choice for this iteration.** This is
> intentionally insecure and is for learning/demo only. The `password` field is a raw string — no
> hashing, no salting, no encryption. **Do not use this design in production.** Hardening (BCrypt/
> Argon2 hashing) is called out as future work in §Notes below.

## Document fields

| Field | BSON type | Constraints | Notes |
|-------|-----------|-------------|-------|
| `_id` | ObjectId | primary key, auto-generated | Credential id (distinct from `customerId`). |
| `email` | string | required, unique, must contain `@` | Login identifier. Mirrors the customer's email. |
| `password` | string | required, non-blank, min length 6 | **Plain text (not hashed)** — see warning above. Never returned in any API response. |
| `customerId` | string | required | Reference to `customerdb.customers._id`. Set from the Customer created/validated during register. |
| `createdAt` | date | set on insert | Audit. |

## Indexes

| Index | Fields | Type | Reason |
|-------|--------|------|--------|
| `_id_` | `_id` | default | Primary key. |
| `uk_email` | `email` | unique | One credential per email → `DuplicateCredentialException` (HTTP 409). |
| `uk_customerId` | `customerId` | unique | One credential per customer. |

## Relationship to Customer Service

- **Customer Service** remains the source of truth for customer *identity* (`name`, `email`, `phone`).
- **Auth Service** is the source of truth for *credentials* (`email`, `password`) and links each
  credential to a `customerId`.
- `email` is intentionally duplicated across both collections: it is the natural login key here and
  the unique customer key there. Register keeps them consistent (same email written to both).

## Example document

```json
{
  "_id": { "$oid": "66aa0011c3d4e5f601234599" },
  "email": "abhinav@example.com",
  "password": "s3cret-plain",
  "customerId": "6699a1f2c3d4e5f601234567",
  "createdAt": { "$date": "2026-07-16T10:10:00Z" }
}
```

## Notes

- **Future hardening (out of scope for this iteration):** hash `password` with BCrypt/Argon2, add
  password-reset + lockout, rotate the JWT signing key, move the shared secret to a vault.
- No refresh-token collection in this iteration — login issues a single short-lived access JWT
  (see `api.md` §JWT contract).
