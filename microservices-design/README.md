# T&F International Bank — Microservices Design

This folder holds the **design artifacts** for converting the single-process console banking app
(`src/bankingRoot/bank/`) into 5 independently deployable microservices.

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

Supporting infrastructure (not counted among the 5 business services):

| Component | Port | Role |
|-----------|------|------|
| Eureka discovery server | 8761 | Service registry |
| API Gateway (Spring Cloud Gateway) | 8080 | Single client entry point / routing |

## Dependency graph (acyclic)

```
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
- **Account** calls Customer (validate) + Transaction (record).
- **Wallet** calls Customer (validate) + Transaction (record) + Notification (limit-exceeded log).

## Mapping from the legacy console app

| Legacy source | Moves to |
|---------------|----------|
| `model/CustomerService.java`, `model/Customer.java` | Customer Service |
| `model/BankAccount`, `SavingsAccount`, `CurrentAccount`, `service/CreateAccount`/`DepositMoney`/`WithdrawMoney`/`TransferMoney` | Account Service |
| `model/Transaction.java`, `util/TransactionList.java`, `service/ViewTransactions.java` | Transaction Service |
| `wallet/*`, `service/WalletOps.java` | Wallet Service |
| `util/FileLogger.java` | Notification/Logging Service |
| `exception/*` | Split to owning service, mapped via `@RestControllerAdvice` |

## Key changes vs the monolith

1. **No global static state.** `Main.currCustomer`/`currAccount` become IDs passed on each request;
   static counters (`Customer.customerCounter`, `Transaction.counter`) → Mongo `ObjectId`.
2. **`Customer extends CustomerService` mislayering removed** — Customer is a plain document; the
   registry becomes the service layer + Mongo repository.
3. **I/O stripped from domain logic** — no `Scanner`/`System.out` in models/services.
4. **Cross-service references by id only** — no shared entity classes; the empty `PaymentType`
   marker interface disappears (accounts vs wallets are separate services).
