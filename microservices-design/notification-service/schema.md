# Notification / Logging Service — MongoDB Schema

- **Database:** `notificationdb`
- **Collection:** `logs`
- **Port:** 8085
- **Ported from:** `util/FileLogger.java`

## Document fields

| Field | BSON type | Constraints | Notes |
|-------|-----------|-------------|-------|
| `_id` | ObjectId | primary key, auto-generated | |
| `ownerId` | string | optional | Customer id the event relates to (was the per-customer `{customerId}.txt` file name). |
| `level` | string (enum) | `INFO` \| `WARN` \| `ERROR` | Was the hardcoded `"[ERROR] "` prefix. |
| `source` | string | required | Originating service, e.g. `wallet-service`. |
| `message` | string | required | The log/notification text. |
| `timestamp` | date | set on insert | |

## Indexes

| Index | Fields | Type | Reason |
|-------|--------|------|--------|
| `_id_` | `_id` | default | Primary key. |
| `ix_owner` | `ownerId` | single | Fetch all events for a customer. |

## Example document

```json
{
  "_id": { "$oid": "6699a3bbccddeeff01234569" },
  "ownerId": "6699a1f2c3d4e5f601234567",
  "level": "ERROR",
  "source": "wallet-service",
  "message": "Wallet has exceeded the maximum amount",
  "timestamp": { "$date": "2026-07-16T10:25:00Z" }
}
```

## Notes

- Replaces `FileLogger`, which appended `"[ERROR] " + message` to
  `./src/util/Transactions/{customerId}.txt` — a **broken hardcoded relative path** that did not
  match the real source root. Events now persist to MongoDB (and optionally a Logback file appender).
- Currently only invoked on Paytm wallet limit-exceeded; the schema generalizes to any service event.
