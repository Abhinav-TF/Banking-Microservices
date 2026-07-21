# Auth Service — API

- **Base URL (direct):** `http://localhost:8086`
- **Via gateway:** `http://localhost:8080/auth/**`
- **Role:** issues and defines JWTs for the whole system. Owns `authdb.credentials`.
  Calls Customer Service on registration. **Not a leaf** (Auth → Customer).

> ⚠️ Passwords are stored and compared in **plain text** by design (demo only). See `schema.md`.

## Public vs protected

- **Public (no JWT required):** `POST /auth/register`, `POST /auth/login`. The gateway lets these
  through without a token.
- **Every other endpoint in the system** requires a valid `Authorization: Bearer <jwt>` (validated
  at the gateway — see `infra/gateway-and-discovery.md`).

## Endpoints exposed

### `POST /auth/register` — create a customer + credential
The onboarding front door. Creates the Customer, then stores the credential linked to it.

Request:
```json
{ "name": "Abhinav Singh", "email": "abhinav@example.com", "phone": "9876543210", "password": "s3cret-plain" }
```
Flow:
1. Validate email/phone/password.
2. **Calls** `POST /customers` on Customer Service → returns the new `customerId`.
3. Insert `credentials` document `{ email, password, customerId }`.

Response `201 Created` (password is **never** returned):
```json
{ "customerId": "6699a1f2c3d4e5f601234567", "email": "abhinav@example.com", "name": "Abhinav Singh" }
```
Errors: `409` duplicate email (in Auth or Customer) · `400` invalid email / phone / weak password.

### `POST /auth/login` — authenticate and get a JWT
Request:
```json
{ "email": "abhinav@example.com", "password": "s3cret-plain" }
```
Flow: look up credential by `email`; compare `password` as a **plain string**; on match, sign a JWT.

Response `200`:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "customerId": "6699a1f2c3d4e5f601234567"
}
```
Errors: `401 Unauthorized` — unknown email or wrong password (same message for both, no user enumeration).

### `GET /auth/me` — current principal (optional convenience)
Reads the gateway-injected `X-Customer-Id` header (already authenticated) and returns the credential
summary. Response `200`: `{ "customerId": "...", "email": "..." }`. Useful for the client to confirm
who it is logged in as.

## JWT contract

- **Algorithm:** `HS512` (HMAC-SHA512) with a shared secret `jwt.secret` present in **both**
  `auth-service` and `api-gateway` config. jjwt auto-selects the HMAC variant from the key length;
  the shared secret is ≥64 bytes, so it signs with HS512. (Pin explicitly or use a 32-byte key for
  HS256; RSA/JWKS is future work.)
- **Claims:**

| Claim | Value |
|-------|-------|
| `sub` | `customerId` |
| `email` | login email |
| `iat` | issued-at (epoch seconds) |
| `exp` | `iat + 3600` (1-hour expiry) |

- The gateway validates signature + `exp` on every protected request and forwards the identity
  downstream as headers `X-Customer-Id` (= `sub`) and `X-Auth-Email`.

## Endpoints called (outbound)

| Target service | URL | When |
|----------------|-----|------|
| Customer | `POST /customers` | On `POST /auth/register`, to create the customer and obtain `customerId`. |

## Error mapping (`@RestControllerAdvice`)

| Exception / condition | HTTP status |
|-----------------------|-------------|
| `DuplicateCredentialException` (email already registered) | 409 Conflict |
| `FeignException` — customer duplicate propagated (409) / other reject | mirrors customer-service (409 / 400) |
| `MethodArgumentNotValidException` — bean validation: invalid email (`@Email`), phone (`@Pattern`), weak password (`@Size` min 6) | 400 Bad Request |
| `InvalidCredentialsException` (bad login) | 401 Unauthorized |
