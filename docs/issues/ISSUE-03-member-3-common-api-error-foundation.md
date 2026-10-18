# [ISSUE-03] Centralized API Error Handling, Bean Validation & Conventions Foundation

## Objective
Standardize the CampusHub backend API foundation before major business modules such as Profile, Community, Marketplace, and Lost & Found are implemented.

This task focuses on consistent API error handling, validation responses, exception mapping, and reusable backend conventions so future modules do not each invent their own response format.

---

## Scope
Implement a reusable backend foundation for:
- Global exception handling (`@RestControllerAdvice`)
- Validation error handling (`MethodArgumentNotValidException`)
- Standard API error response format (`ApiErrorResponse`)
- HTTP status conventions
- Common backend exceptions (`BadRequestException`, `ConflictException`, `ResourceNotFoundException`)
- Security / Information safety (safe 500 fallback)
- Package/convention documentation for future modules ([`docs/backend-conventions.md`](../backend-conventions.md))

---

## Requirements

### Global Exception Handling
Create a centralized exception handling mechanism using Spring `@RestControllerAdvice` in [`GlobalExceptionHandler.java`](../../backend/src/main/java/com/campushub/common/exception/GlobalExceptionHandler.java).

Handle at minimum:
- Request validation errors (`MethodArgumentNotValidException`) $\rightarrow$ HTTP 400
- Invalid request / bad input (`BadRequestException`) $\rightarrow$ HTTP 400
- Resource not found (`ResourceNotFoundException`) $\rightarrow$ HTTP 404
- Conflict / business conflict (`ConflictException`) $\rightarrow$ HTTP 409
- Unexpected server errors (`Exception.class`) $\rightarrow$ HTTP 500

### Standard Error Response
Define and use a consistent error structure across the backend in [`ApiErrorResponse.java`](../../backend/src/main/java/com/campushub/common/dto/ApiErrorResponse.java):

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fields": {
    "email": "Invalid email"
  },
  "timestamp": "2026-10-11T12:00:00"
}
```

- Reusable static factory methods:
  - `ApiErrorResponse.of(int status, String error, String message)`
  - `ApiErrorResponse.of(int status, String error, String message, Map<String, String> fields)`
- Ensure ISO-8601 formatting for timestamps.

### Validation Errors
- Map Bean Validation errors (`@Valid`) into clear field-level messages (`Map<String, String> fields`).
- Do not leak framework/internal exception details or reflection traces.
- Keep responses frontend-friendly.
- Support multiple invalid fields in one response.

### Common Exceptions
Introduce reusable application exceptions in `com.campushub.common.exception`:
- [`ResourceNotFoundException.java`](../../backend/src/main/java/com/campushub/common/exception/ResourceNotFoundException.java)
- [`ConflictException.java`](../../backend/src/main/java/com/campushub/common/exception/ConflictException.java)
- [`BadRequestException.java`](../../backend/src/main/java/com/campushub/common/exception/BadRequestException.java)
- Delete untracked duplicate [`AccessDeniedException.java`](../../backend/src/main/java/com/campushub/common/exception/AccessDeniedException.java) to prevent conflicting with Spring Security's native class.

### Security / Error Safety
- Do not expose stack traces to clients.
- Do not expose SQL/database internals or table names.
- Do not expose sensitive authentication details.
- Preserve correct distinction between 401 and 403 (handled at the filter level by Member 2).
- Unexpected server errors must return a safe generic message: `"An unexpected error occurred"`.
- Log original exceptions server-side using SLF4J (`log.error(...)`).

### Backend Conventions Documentation
Maintain and expand [`docs/backend-conventions.md`](../backend-conventions.md):
- Layering standards:
  - `controller/` $\rightarrow$ handles HTTP, validation, returns DTOs.
  - `service/` $\rightarrow$ business logic, throws semantic exceptions.
  - `repository/` $\rightarrow$ persistence via Spring Data JPA.
  - `entity/` $\rightarrow$ JPA entities, extends `BaseEntity`.
  - `dto/` $\rightarrow$ request/response models.
  - `exception/` $\rightarrow$ shared application exceptions.
  - `config/` / `security/` $\rightarrow$ cross-cutting configurations.
- Entities must never be returned directly in controller signatures.

### Testing
- Maintain unit tests in [`ApiErrorResponseTest.java`](../../backend/src/test/java/com/campushub/common/dto/ApiErrorResponseTest.java).
- Maintain controller advice tests in [`GlobalExceptionHandlerTest.java`](../../backend/src/test/java/com/campushub/common/exception/GlobalExceptionHandlerTest.java).
- Coordinate with Member 4 to remove standalone 401/403 tests from `GlobalExceptionHandlerTest` (as those belong to Member 4's filter tests).

---

## Notes
- Do not implement Community, Marketplace, Lost & Found, or Profile business logic in this issue.
- Keep this task focused on reusable backend infrastructure.
- Coordinate with Member 2 to ensure filter-level error responses use this exact DTO contract.

---

## Deliverables
- [`GlobalExceptionHandler.java`](../../backend/src/main/java/com/campushub/common/exception/GlobalExceptionHandler.java).
- [`ApiErrorResponse.java`](../../backend/src/main/java/com/campushub/common/dto/ApiErrorResponse.java).
- Reusable application exceptions (`ResourceNotFoundException`, `ConflictException`, `BadRequestException`).
- [`docs/backend-conventions.md`](../backend-conventions.md).
- [`ApiErrorResponseTest.java`](../../backend/src/test/java/com/campushub/common/dto/ApiErrorResponseTest.java).
- PR targeting `developV2`.

---

## Definition of Done
- [x] Global exception handling is centralized.
- [x] Validation errors return a consistent frontend-friendly format with field-level dictionary.
- [x] 400, 404, 409, and 500 behaviors are clearly defined.
- [x] Stack traces and database internals are not exposed to clients.
- [x] Reusable application exception approach exists.
- [x] Backend conventions are documented in `docs/backend-conventions.md`.
- [ ] Redundant duplicate exception classes are deleted.
- [ ] Unit tests pass cleanly.
- [ ] PR submitted to `developV2`.

---

## Suggested Branch
`feature/SE-001-backend-api-foundation`

## Suggested Commit
`feat(backend): standardize api error handling foundation`

