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

## Inter-service communication

- **Synchronous REST** via OpenFeign declarative clients (or `WebClient`), targeting service names
  resolved through Eureka — e.g. Account Service's Feign client points at `customer-service`, not a
  hardcoded URL.
- Call graph (see repo `README.md` for the diagram): Account → Customer, Transaction;
  Wallet → Customer, Transaction, Notification. Customer, Transaction, Notification are leaves.

## Startup order (recommended)

1. Eureka discovery server (`:8761`).
2. Leaf services: `customer-service`, `transaction-service`, `notification-service`.
3. Dependent services: `account-service`, `wallet-service`.
4. `api-gateway` (`:8080`).

## MongoDB

- One `mongod` instance is sufficient locally; each service targets its **own database**
  (`customerdb`, `accountdb`, `transactiondb`, `walletdb`, `notificationdb`) — no shared collections.
- Each service sets `spring.data.mongodb.database` accordingly in its `application.yml`.
