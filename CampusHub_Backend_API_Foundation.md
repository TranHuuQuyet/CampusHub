# CampusHub — Backend API Foundation

## Issue

**Standardize Backend Errors**

## Objective

Standardize the CampusHub backend API foundation before major business modules such as Profile, Community, Marketplace, and Lost & Found are implemented.

This task establishes reusable conventions for:

- Global exception handling
- Validation error handling
- Standard API error responses
- HTTP status conventions
- Common application exceptions
- Security-safe error responses
- Audit timestamps where appropriate
- Backend package/convention documentation
- Automated tests for the shared foundation

> **Scope rule:** Do not implement Community, Marketplace, Lost & Found, or Profile business logic in this issue.

---

# 1. Implementation Flow

```text
Inspect current backend
        ↓
Inspect existing exception / validation / security handling
        ↓
Design common API error contract
        ↓
Create reusable error response DTO
        ↓
Create common application exceptions
        ↓
Implement GlobalExceptionHandler
        ↓
Integrate validation error mapping
        ↓
Standardize 401 / 403 security errors
        ↓
Implement safe unexpected-error handling (500)
        ↓
Evaluate audit timestamp foundation
        ↓
Document backend conventions
        ↓
Add tests
        ↓
Run full test suite
        ↓
Manual API verification
        ↓
Review Git diff / scope
        ↓
Commit + Push
        ↓
Create PR → develop
        ↓
Code review
        ↓
Fix review comments
        ↓
Re-run tests
        ↓
Merge
```

---

# 2. Task 1 — Inspect Existing Backend

## 1.1 Inspect backend structure

### Goal

Understand the current CampusHub backend architecture before creating or moving anything.

### Do not modify code yet.

Inspect:

```text
src/main/java/
src/test/java/
pom.xml
```

Useful commands on Windows:

```cmd
tree src\main\java /F
tree src\test\java /F
```

Inspect the existing package structure, especially:

```text
controller/
service/
repository/
model/
entity/
dto/
exception/
config/
security/
```

### Questions to answer

- Does an `exception` package already exist?
- Does an error response DTO already exist?
- Is there already a global exception handler?
- Are controllers/services/repositories consistently organized?
- How are tests currently organized?
- Are authentication components isolated from general application code?

### Result

Document the actual existing structure before making changes.

---

# 3. Task 2 — Inspect Existing Error Handling

Search for:

```text
@RestControllerAdvice
@ControllerAdvice
@ExceptionHandler
ResponseStatusException
RuntimeException
IllegalArgumentException
```

Windows example:

```cmd
findstr /S /I /N "ControllerAdvice ExceptionHandler ResponseStatusException" src\main\java\*.java
```

Then identify:

- Existing exception classes
- Existing HTTP status handling
- Existing error DTOs
- Existing controller-level exception handling
- Duplicated error-response logic

### Rule

If CampusHub already has an appropriate mechanism, extend it instead of creating a competing implementation.

---

# 4. Task 3 — Inspect Validation

Search for:

```text
@Valid
@Validated
@NotNull
@NotBlank
@NotEmpty
@Email
@Size
@Pattern
```

Example:

```cmd
findstr /S /I /N "@Valid @Validated @NotNull @NotBlank @Email @Size" src\main\java\*.java
```

Inspect `pom.xml` and verify whether Bean Validation is already configured.

Determine:

- Which DTOs use validation
- Which validation annotations are already used
- Whether validation errors are currently handled
- Whether multiple invalid fields can be returned

---

# 5. Task 4 — Inspect Authentication / Security

Before changing security, inspect the existing authentication flow.

Search for:

```text
SecurityFilterChain
AuthenticationEntryPoint
AccessDeniedHandler
JWT
AuthenticationFilter
```

Example:

```cmd
findstr /S /I /N "SecurityFilterChain AuthenticationEntryPoint AccessDeniedHandler" src\main\java\*.java
```

### Required behavior

```text
Unauthenticated
    ↓
401 Unauthorized

Authenticated but insufficient permission
    ↓
403 Forbidden
```

### Important

Do not perform large-scale authentication refactoring.

Only change authentication/security code where required to integrate the shared error response.

Coordinate changes carefully because authentication integration may be handled by another issue.

---

# 6. Task 5 — Design the Common API Error Contract

Before implementing the handler, define one reusable response structure.

Recommended direction:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fields": {
    "email": "Invalid email"
  },
  "timestamp": "2026-10-01T14:00:00"
}
```

For errors without field-level validation:

```json
{
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "User not found",
  "timestamp": "2026-10-01T14:00:00"
}
```

For unexpected errors:

```json
{
  "status": 500,
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "timestamp": "2026-10-01T14:00:00"
}
```

### Decide

- Field names
- Java types
- Timestamp representation
- Whether `fields` is optional/null/empty
- Error-code naming convention

Once decided, all backend modules should use the same contract.

---

# 7. Task 6 — Implement Reusable API Error Response

Create a reusable DTO, for example:

```text
dto/
└── error/
    └── ApiErrorResponse.java
```

Conceptually:

```text
ApiErrorResponse
├── status
├── error
├── message
├── fields
└── timestamp
```

### Rule

Future modules must not create their own error response models such as:

```text
CommunityErrorResponse
MarketplaceErrorResponse
LostItemErrorResponse
ProfileErrorResponse
```

All modules should use the shared error contract.

---

# 8. Task 7 — Implement Common Application Exceptions

Create only exceptions that provide useful semantic meaning.

Possible examples:

```text
ResourceNotFoundException
ConflictException
InvalidBusinessOperationException
```

Example:

```java
throw new ResourceNotFoundException("Post not found");
```

Expected flow:

```text
Service
  ↓
throws application exception
  ↓
GlobalExceptionHandler
  ↓
ApiErrorResponse
  ↓
HTTP response
```

The service layer should not manually construct HTTP error responses.

---

# 9. Task 8 — Implement Global Exception Handler

Create or extend:

```text
GlobalExceptionHandler
```

using:

```java
@RestControllerAdvice
```

At minimum handle:

| Scenario | HTTP | Error Code |
|---|---:|---|
| Validation failure | 400 | `VALIDATION_ERROR` |
| Invalid request | 400 | `BAD_REQUEST` |
| Unauthorized | 401 | `UNAUTHORIZED` |
| Forbidden | 403 | `FORBIDDEN` |
| Resource not found | 404 | `RESOURCE_NOT_FOUND` |
| Business/resource conflict | 409 | `CONFLICT` |
| Unexpected server error | 500 | `INTERNAL_SERVER_ERROR` |

---

# 10. Task 9 — Validation Error Mapping

Validation failures must be frontend-friendly.

Example:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fields": {
    "email": "Invalid email",
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2026-10-01T14:00:00"
}
```

Requirements:

- Return field-level messages
- Support multiple invalid fields
- Do not expose framework internals
- Keep the response consistent
- Avoid exposing stack traces

---

# 11. Task 10 — Standardize 401 / 403

Spring Security may handle 401/403 before requests reach `@RestControllerAdvice`.

Therefore inspect and, where necessary, update:

```text
AuthenticationEntryPoint
AccessDeniedHandler
```

Both should return the shared error format.

## 401

```text
No valid authentication
        ↓
AuthenticationEntryPoint
        ↓
401 Unauthorized
```

## 403

```text
Authenticated user
        ↓
Authorization denied
        ↓
AccessDeniedHandler
        ↓
403 Forbidden
```

Do not confuse:

```text
401 = authentication is missing/invalid
403 = authentication exists but access is forbidden
```

---

# 12. Task 11 — Unexpected Error Handling

Handle unexpected exceptions centrally.

The client must receive a safe response such as:

```json
{
  "status": 500,
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "timestamp": "2026-10-01T14:00:00"
}
```

Do NOT expose:

- Stack traces
- SQL statements
- Database schema details
- Hibernate internals
- Framework exception details
- Sensitive authentication information

Internal logs may contain the original exception for debugging.

---

# 13. Task 12 — Audit Timestamp Foundation

Evaluate whether a reusable timestamp foundation fits the current architecture.

Possible direction:

```text
BaseEntity
├── createdAt
└── updatedAt
```

A reusable base entity may be introduced if it can be done safely.

### Do not force this change.

If introducing audit timestamps would require:

- Broad entity refactoring
- Risky database changes
- Authentication refactoring
- Large unrelated changes

then defer the implementation.

Document the reason clearly.

---

# 14. Task 13 — Backend Conventions Documentation

Create or update documentation such as:

```text
docs/backend-conventions.md
```

Document:

## Package convention

```text
controller/
service/
repository/
entity/ or model/
dto/
exception/
config/
security/
```

## Request flow

```text
Controller
    ↓
Service
    ↓
Repository
```

## Error flow

```text
Application Exception
    ↓
GlobalExceptionHandler
    ↓
ApiErrorResponse
    ↓
HTTP Response
```

## Rules

- Controllers should not contain business logic.
- Services contain business rules.
- Repositories handle persistence.
- DTOs define API contracts.
- Entities should not normally be exposed directly through APIs.
- Shared application exceptions should be used where appropriate.
- All API errors use the shared error response.
- New modules must not invent their own error response format.

---

# 15. Task 14 — Tests

Create tests for the shared foundation.

Recommended coverage:

```text
[ ] Validation failure → 400
[ ] Multiple validation errors → field map
[ ] Bad request → 400
[ ] Unauthorized → 401
[ ] Forbidden → 403
[ ] Resource not found → 404
[ ] Conflict → 409
[ ] Unexpected exception → 500
[ ] 500 response does not expose stack trace
[ ] 500 response does not expose SQL/database internals
[ ] Existing authentication tests still pass
```

Prefer testing through the actual HTTP layer where appropriate, e.g. `MockMvc`, so the complete error response can be verified.

---

# 16. Task 15 — Run Full Test Suite

Windows:

```cmd
mvnw.cmd test
```

or:

```cmd
.\mvnw.cmd test
```

Do not only run the newly created tests.

Verify:

```text
New foundation tests
        +
Existing backend tests
        +
Authentication tests
        ↓
ALL PASS
```

---

# 17. Task 16 — Manual API Verification

Run the backend and manually verify representative cases:

```text
400
401
403
404
409
500
```

Check:

- HTTP status
- Error code
- Message
- Validation fields
- Timestamp
- No sensitive/internal information

---

# 18. Task 17 — Scope and Git Review

Before committing:

```cmd
git status
git diff
```

Check for accidental modifications to:

```text
Authentication
JWT
Security configuration
Database schema
Existing entities
Existing business logic
Unrelated controllers/services
```

Ask:

> Did this issue change anything that was not necessary for the shared backend foundation?

Remove unrelated changes.

---

# 19. Task 18 — Commit

Use a focused commit message.

Example:

```cmd
git add .
git commit -m "feat(backend): standardize API error handling"
```

Push the feature branch:

```cmd
git push origin <branch-name>
```

---

# 20. Task 19 — Pull Request

Target:

```text
develop
```

### PR Summary

```text
- Added centralized API exception handling
- Added reusable API error response
- Added validation error mapping
- Added common application exceptions
- Standardized 401/403 responses
- Added tests for shared error handling
- Added backend conventions documentation
```

### Testing

```text
mvnw.cmd test

Result: PASS
```

### Audit Timestamp

Explicitly state:

```text
Implemented
```

or:

```text
Deferred and documented
```

---

# 21. Task 20 — Lead Review

Review the PR in this order:

```text
1. Architecture
2. Error response consistency
3. Validation handling
4. 401 / 403 security behavior
5. 404 / 409 exception mapping
6. 500 information safety
7. Tests
8. Authentication compatibility
9. Documentation
10. Unnecessary changes / scope creep
```

The key question:

> Can a developer implementing Community, Marketplace, Profile, or Lost & Found use this foundation without creating another error-handling system?

If yes, the foundation is serving its purpose.

---

# 22. Definition of Done

```text
[ ] Global exception handling is centralized
[ ] Reusable ApiErrorResponse exists
[ ] Validation errors have field-level messages
[ ] 400 behavior is defined
[ ] 401 behavior is defined
[ ] 403 behavior is defined
[ ] 404 behavior is defined
[ ] 409 behavior is defined
[ ] 500 behavior is defined
[ ] Stack traces are not exposed
[ ] SQL/database internals are not exposed
[ ] Sensitive authentication details are not exposed
[ ] Reusable application exception approach exists
[ ] Backend package conventions are documented
[ ] Audit timestamp foundation is implemented if appropriate
[ ] Audit timestamp deferral is documented if not implemented
[ ] Shared error-handling tests exist
[ ] Authentication tests still pass
[ ] Full Maven test suite passes
[ ] Changes are manually verified
[ ] No unrelated business logic was added
[ ] PR targets develop
[ ] PR review completed
[ ] Review comments resolved
[ ] Final tests pass
[ ] PR merged
```

---

# 23. Out of Scope

This issue must NOT implement:

- Community business logic
- Marketplace business logic
- Lost & Found business logic
- Profile business logic
- New authentication features
- Large-scale authentication refactoring
- Unrelated entity/database refactoring
- Feature-specific error response formats

The goal is **shared backend infrastructure only**.

---

# 24. Expected End State

After this issue is merged, a future module should be able to do:

```java
throw new ResourceNotFoundException("Post not found");
```

and automatically receive:

```json
{
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Post not found",
  "timestamp": "..."
}
```

without implementing its own exception handler.

That is the core outcome of this issue.
