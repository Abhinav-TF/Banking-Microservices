# T&F International Bank — Microservices Design

This folder holds the **design artifacts** for converting the single-process console banking app
(`src/bankingRoot/bank/`) into 6 independently deployable microservices (5 business + 1 auth).

- **Stack:** Spring Boot + Spring Cloud (Eureka discovery + Spring Cloud Gateway)
- **Persistence:** MongoDB, **one database per service** (Spring Data MongoDB)
- **Communication:** synchronous REST (OpenFeign, resolved by service name via Eureka)

> This is documentation only — no service code is generated yet. Each service folder contains a
> `schema.md` (MongoDB document design) and an `api.md` (endpoints exposed + endpoints called).

## Service map

| # | Service | Mongo DB | Collection | Port | Depends on |
|---|---------|----------|------------|------|------------|
| 1 | Customer | `customerdb` | `customers` | 8081 | — (leaf) |
| 2 | Account | `accountdb` | `accounts` | 8082 | Customer, Transaction |
| 3 | Transaction | `transactiondb` | `transactions` | 8083 | — (leaf) |
| 4 | Wallet | `walletdb` | `wallets` | 8084 | Customer, Transaction, Notification |
| 5 | Notification/Logging | `notificationdb` | `logs` | 8085 | — (leaf) |
| 6 | Auth | `authdb` | `credentials` | 8086 | Customer |

> **Auth Service** is a platform/security service (not one of the original 5 business services). It
> stores login credentials and issues JWTs. ⚠️ **Passwords are stored in plain text by design for
> this iteration** — demo/learning only, never production. See `auth-service/schema.md`.

Supporting infrastructure (not counted among the 5 business services):

| Component | Port | Role |
|-----------|------|------|
| Eureka discovery server | 8761 | Service registry |
| API Gateway (Spring Cloud Gateway) | 8080 | Single client entry point / routing |

## Dependency graph (acyclic)

```
        Auth ────────────────────────────┐ create customer (register)
                                          ▼
        Customer (leaf) ◄───────────────┐
                                        │ validate owner
   Transaction (leaf) ◄──────┐          │
                             │ record   │
        Account ─────────────┴──────────┘
                             │ record        │ validate owner
        Wallet ──────────────┴───────────────┘
                             │ log on limit-exceeded
   Notification (leaf) ◄──────┘
```

- **Leaves (no outbound calls):** Customer, Transaction, Notification.
- **Auth** calls Customer (`POST /customers` on register).
- **Account** calls Customer (validate) + Transaction (record).
- **Wallet** calls Customer (validate) + Transaction (record) + Notification (limit-exceeded log).

## Authentication & security

- **Every client request enters through the gateway with a JWT** (except `POST /auth/register` and
  `POST /auth/login`, which are public). The gateway validates the JWT and forwards the caller's
  identity to downstream services as trusted headers.
- **JWT is issued by Auth Service** (`HS256`, shared `jwt.secret`, `sub = customerId`, 1-hour expiry)
  and **validated at the API Gateway**. See `auth-service/api.md` §JWT contract and
  `infra/gateway-and-discovery.md` §JWT validation filter.
- **Downstream services trust the gateway, not the client.** The gateway strips any client-supplied
  `X-Customer-Id` and re-injects `X-Customer-Id` / `X-Auth-Email` from the verified token. Account,
  Wallet and Transaction take the acting customer from `X-Customer-Id` (ownership checks) instead of
  trusting a `customerId` in the request body/query. See each service's `api.md` §Authentication.
- ⚠️ **Plain-text passwords** are a deliberate simplification for this iteration only.

## Mapping from the legacy console app

| Legacy source | Moves to |
|---------------|----------|
| `model/CustomerService.java`, `model/Customer.java` | Customer Service |
| `model/BankAccount`, `SavingsAccount`, `CurrentAccount`, `service/CreateAccount`/`DepositMoney`/`WithdrawMoney`/`TransferMoney` | Account Service |
| `model/Transaction.java`, `util/TransactionList.java`, `service/ViewTransactions.java` | Transaction Service |
| `wallet/*`, `service/WalletOps.java` | Wallet Service |
| `util/FileLogger.java` | Notification/Logging Service |
| `exception/*` | Split to owning service, mapped via `@RestControllerAdvice` |
| _(no legacy equivalent — console app had no login)_ | Auth Service (new) |

## Key changes vs the monolith

1. **No global static state.** `Main.currCustomer`/`currAccount` become IDs passed on each request;
   static counters (`Customer.customerCounter`, `Transaction.counter`) → Mongo `ObjectId`.
2. **`Customer extends CustomerService` mislayering removed** — Customer is a plain document; the
   registry becomes the service layer + Mongo repository.
3. **I/O stripped from domain logic** — no `Scanner`/`System.out` in models/services.
4. **Cross-service references by id only** — no shared entity classes; the empty `PaymentType`
   marker interface disappears (accounts vs wallets are separate services).
