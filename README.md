# Cheque Processing Service

Spring Boot service to issue and present cheques with:
- Mandatory SAYAD registration/presentation (stubbed HTTP calls)
- Non-bearer and unconditional rule checks
- Six-month presentation window
- Bounce handling and account blocking after 3 bounces in 12 months
- JWT-secured endpoints (role: TELLER)
- H2 in-memory DB

## Build & Run

```bash
./mvnw spring-boot:run
```

H2 console: `/h2`
- JDBC URL: `jdbc:h2:mem:cheques`
- User: `sa`
- Password: (empty)

## Auth (JWT)

Generate a JWT token using any JWT tool or use this sample token for testing:
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWxsZXIxIiwicm9sZSI6IlJPTEVfVEVMTEVSIiwiaWF0IjoxNzU4ODg1MzI2LCJleHAiOjE3NTg4ODg5MjZ9.OpmbyQYNgEuQDIldbwqf82oIEvjPN6yccQFQVXOLH5A
```

Use in requests:
```
Authorization: Bearer <JWT>
```

**Note**: For production, implement proper authentication. This demo uses a hardcoded token for simplicity.

## Endpoints

- Issue cheque
```bash
curl -X POST http://localhost:8080/api/cheques \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWxsZXIxIiwicm9sZSI6IlJPTEVfVEVMTEVSIiwiaWF0IjoxNzU4ODg1MzI2LCJleHAiOjE3NTg4ODg5MjZ9.OpmbyQYNgEuQDIldbwqf82oIEvjPN6yccQFQVXOLH5A" \
  -H "Content-Type: application/json" \
  -d '{"drawerId":1, "number":"YT-2025-0001", "amount":150000.00}'
```

- Present cheque
```bash
curl -X POST http://localhost:8080/api/cheques/<CHEQUE_ID>/present \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWxsZXIxIiwicm9sZSI6IlJPTEVfVEVMTEVSIiwiaWF0IjoxNzU4ODg1MzI2LCJleHAiOjE3NTg4ODg5MjZ9.OpmbyQYNgEuQDIldbwqf82oIEvjPN6yccQFQVXOLH5A"
```

## Tests

```bash
./mvnw test
```
JaCoCo report: `target/site/jacoco/index.html`

## Config

- SAYAD base URL: `sayad.base-url` (default `http://localhost:8081/sayad`)
- JWT:
  - `security.jwt.secret`
  - `security.jwt.expirationSeconds`

## Notes
- Integration tests use WireMock to stub SAYAD HTTP calls.
- Accounts are seeded on startup for demo purposes.
- JWT tokens can be generated using any JWT tool (e.g., jwt.io) with the secret from application.yml.
