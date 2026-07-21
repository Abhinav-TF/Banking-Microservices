# Infrastructure — API Gateway & Service Discovery

Supporting Spring Cloud components. Not business services, but required to run the system.

## Eureka Discovery Server — `:8761`

- Netflix Eureka server. Every business service registers on startup with its `spring.application.name`.
- Services resolve each other **by name** (not host/port), so ports can change freely.
- Dashboard: `http://localhost:8761` — lists all registered instances.

Registered application names:

| Application name | Port |
|------------------|------|
| `customer-service` | 8081 |
| `account-service` | 8082 |
| `transaction-service` | 8083 |
| `wallet-service` | 8084 |
| `notification-service` | 8085 |
| `auth-service` | 8086 |
| `api-gateway` | 8080 |

## API Gateway (Spring Cloud Gateway) — `:8080`

Single client entry point. Routes by path prefix to the service registered under each name
(load-balanced via `lb://`). The old CLI `Main` becomes a client of the gateway.

| Route predicate (path) | Forwards to |
|------------------------|-------------|
| `/customers/**` | `lb://customer-service` |
| `/accounts/**` | `lb://account-service` |
| `/transactions/**` | `lb://transaction-service` |
| `/wallets/**` | `lb://wallet-service` |
| `/notifications/**`, `/logs/**` | `lb://notification-service` |
| `/auth/**` | `lb://auth-service` |

## JWT validation filter (authentication at the edge)

Auth is enforced **once, at the gateway** — downstream services trust the gateway and do not
re-validate the token.

- **Public paths (no token required):** `POST /auth/register`, `POST /auth/login`. All other paths
  require a valid token.
- **A global gateway filter** runs on every non-public request:
  1. Read `Authorization: Bearer <jwt>`; reject with `401` if missing.
  2. Verify the signature (`HS256`, shared `jwt.secret` — the same secret `auth-service` signs with)
     and the `exp` claim; reject with `401` if invalid or expired.
  3. **Strip any client-supplied `X-Customer-Id` / `X-Auth-Email`** (anti-spoofing), then inject
     `X-Customer-Id` = the JWT `sub` and `X-Auth-Email` = the `email` claim.
  4. Forward the request to the target service.
- **Downstream services** read the acting customer from `X-Customer-Id` (already authenticated) —
  they never trust a `customerId` supplied directly by the client. See each service's `api.md`
  §Authentication.

```
Client ──Authorization: Bearer <jwt>──▶ Gateway
                                          │ verify signature + exp (jwt.secret)
                                          │ strip client X-Customer-Id, inject verified one
                                          ▼
                            X-Customer-Id: <sub> ──▶ customer/account/wallet/... service
```

- **Shared secret:** `jwt.secret` lives in both `auth-service` and `api-gateway` config (env var /
  config server in a real deployment). Rotating to RSA + JWKS is future work.

## Inter-service communication

- **Synchronous REST** via OpenFeign declarative clients (or `WebClient`), targeting service names
  resolved through Eureka — e.g. Account Service's Feign client points at `customer-service`, not a
  hardcoded URL.
- Call graph (see repo `README.md` for the diagram): Account → Customer, Transaction;
  Wallet → Customer, Transaction, Notification. Customer, Transaction, Notification are leaves.

## Startup order (recommended)

1. Eureka discovery server (`:8761`).
2. Leaf services: `customer-service`, `transaction-service`, `notification-service`.
3. Dependent services: `account-service`, `wallet-service`, `auth-service` (Auth needs Customer up
   so `POST /auth/register` can create customers).
4. `api-gateway` (`:8080`).

## MongoDB

- One `mongod` instance is sufficient locally; each service targets its **own database**
  (`customerdb`, `accountdb`, `transactiondb`, `walletdb`, `notificationdb`, `authdb`) — no shared
  collections.
- Each service sets `spring.data.mongodb.database` accordingly in its `application.yml`.
