# Customer Service — API

- **Base URL (direct):** `http://localhost:8081`
- **Via gateway:** `http://localhost:8080/customers/**`
- **Role:** leaf service — exposes customer CRUD + validation; **calls no other service**.

## Authentication

- Client-facing endpoints sit behind the gateway JWT filter and receive a verified `X-Customer-Id`
  header (see `infra/gateway-and-discovery.md`). Reads use it for ownership checks: `GET
  /customers/{id}` returns `401` if the header is absent and `403` if it isn't the caller's own id
  (unless an admin scope is added later); `GET /customers` requires an authenticated caller.
- **Internal exception — `GET /customers/{id}/exists`** is NOT ownership-checked. Account/Wallet call
  it service-to-service (Feign via Eureka, bypassing the gateway), so it carries no `X-Customer-Id`.
  Use this for owner validation, **not** the ownership-scoped `GET /customers/{id}`.
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
{ "id": "6699a1f2c3d4e5f601234567", "name": "Abhinav Singh", "email": "abhinav@example.com", "phone": "9876543210", "createdAt": "2026-07-16T10:15:30Z" }
```
Errors: `409` duplicate email · `400` invalid email / phone.

### `GET /customers/{id}` — fetch one customer
Ported from `CustomerService.getCustomers(int id)`.
Response `200`: single customer document (includes `createdAt`). `404` if not found.
**Ownership-checked** (see §Authentication): `401` if `X-Customer-Id` is absent, `403` if it isn't `{id}`.

### `GET /customers/{id}/exists` — internal owner validation
For Account/Wallet service-to-service validation (not ownership-checked, no `X-Customer-Id` needed).
Response `200`: `{ "id": "6699a1f2c3d4e5f601234567", "exists": true }`.

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
| `MethodArgumentNotValidException` — bean validation: invalid email (`@Email`) / phone (`@Pattern`) / blank fields (`@NotBlank`) | 400 Bad Request |
| `CustomerNotFoundException` (not found) | 404 Not Found |
| `UnauthorizedException` — missing `X-Customer-Id` on an ownership-checked read | 401 Unauthorized |
| `ForbiddenException` — accessing another customer's record | 403 Forbidden |

> `InvalidEmailException` / `InvalidPhoneNumberException` exist and are mapped to 400, but format
> checks are currently enforced by bean validation (`@Email` / `@Pattern`), so those named exceptions
> are not thrown in the current implementation.
