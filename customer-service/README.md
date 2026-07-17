# Customer Service

Leaf microservice for **T&F International Bank** — exposes customer CRUD + validation and calls no
other service. See design docs in `../microservices-design/customer-service/`.

- **Port:** 8081
- **Registers with Eureka as:** `customer-service`
- **Database:** MongoDB `customerdb`, collection `customers`
- **Via gateway:** `http://localhost:8080/customers/**`

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/customers` | Create a customer (201) |
| `GET`  | `/customers/{id}` | Fetch one customer |
| `GET`  | `/customers` | List all customers |
| `GET`  | `/customers?excludeId={id}` | List all customers except one |

## Build & run

```bash
mvn spring-boot:run
```

Requires a running MongoDB (`localhost:27017`) and Eureka server (`localhost:8761`).

## Structure

```
src/main/java/com/tnf/customer/
├── CustomerServiceApplication.java   # entry point (@SpringBootApplication, @EnableDiscoveryClient)
├── controller/CustomerController.java
├── service/CustomerService.java
├── repository/CustomerRepository.java
├── model/Customer.java
├── dto/            # CreateCustomerRequest, CustomerResponse
└── exception/      # domain exceptions + GlobalExceptionHandler (@RestControllerAdvice)
```

> Scaffolding only — method bodies are stubs (`TODO`); no business logic yet.
