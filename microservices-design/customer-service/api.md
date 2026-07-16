# Customer Service — API

- **Base URL (direct):** `http://localhost:8081`
- **Via gateway:** `http://localhost:8080/customers/**`
- **Role:** leaf service — exposes customer CRUD + validation; **calls no other service**.

## Authentication

- All endpoints sit behind the gateway JWT filter and receive a verified `X-Customer-Id` header
  (see `infra/gateway-and-discovery.md`). Reads use it for ownership checks; `GET /customers/{id}`
  should only return the caller's own record unless an admin scope is added later.
- **`POST /customers` is normally called by Auth Service**, not the client directly: `POST
  /auth/register` creates the customer and then stores the credential (see `auth-service/api.md`).
  The endpoint is unchanged, so Auth reuses it as-is.

## Endpoints exposed

### `POST /customers` — create a customer
Ported from `CustomerService.createUser(...)`.

Request:
```json
{ "name": "Abhinav Singh", "email": "abhinav@example.com", "phone": "9876543210" }
```
Response `201 Created`:
```json
{ "id": "6699a1f2c3d4e5f601234567", "name": "Abhinav Singh", "email": "abhinav@example.com", "phone": "9876543210" }
```
Errors: `409` duplicate email · `400` invalid email / phone.

### `GET /customers/{id}` — fetch one customer
Ported from `CustomerService.getCustomers(int id)`.
Response `200`: single customer document. `404` if not found.

### `GET /customers` — list all customers
Ported from `CustomerService.showCustomers()`.
Response `200`: array of customer documents.

### `GET /customers?excludeId={id}` — list all except one
Ported from `CustomerService.showCustomersExcept(int excludeId)` (used by transfer flow to pick a target customer).
Response `200`: array of customer documents excluding `{id}`.

## Endpoints called (outbound)

_None._ Customer Service is a leaf.

## Error mapping (`@RestControllerAdvice`)

| Exception | HTTP status |
|-----------|-------------|
| `DuplicateCustomerException` | 409 Conflict |
| `InvalidEmailException` | 400 Bad Request |
| `InvalidPhoneNumberException` | 400 Bad Request |
| not found | 404 Not Found |
