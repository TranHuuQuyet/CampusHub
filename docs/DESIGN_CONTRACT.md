# CampusHub — Design Contract

**Document Type:** Software Engineering Design & Integration Contract  
**Version:** 1.1  
**Status:** Approved Team Baseline  
**Project:** CampusHub  
**Applies To:** Frontend, Backend, Database, Security, QA, and DevOps  
**Repository Branch:** `developV2`  

---

## 1. Purpose

This document defines the shared technical agreements that CampusHub developers must follow when designing, implementing, testing, and integrating software modules.

Its objectives are to:

- Standardize client–server communication across all modules (Profile, Community, Marketplace, Lost & Found, Notifications, Admin/Moderation).
- Establish consistent JSON request, success-response, and error-response formats matching the existing codebase.
- Define validation, HTTP status, authentication, and authorization conventions.
- Reduce integration conflicts between team members.
- Establish clear module ownership, definition of done, and change-management procedures.
- Enforce security, maintainability, and architectural integrity.

**Core principle:** Every shared interface must have an explicit contract. Developers must not silently introduce incompatible formats, unauthorized entity mutations, or conflicting conventions.

---

## 2. Scope

This contract applies to:

- **Frontend:** React, TypeScript, Vite client components, service layers, and API integration.
- **Backend:** Spring Boot 4.1.1 (Java 21) controllers, services, DTOs, repositories, security filter chain, and centralized exception handling.
- **Database:** MySQL 8.4 entities, tables, relationships, constraints, indexes, and audit timestamps.
- **Authentication & Security:** Stateless JWT Bearer tokens, BCrypt password hashing, role-based access controls (`ROLE_USER`, `ROLE_ADMIN`), and custom 401/403 handlers.
- **Testing & Quality Assurance:** Unit tests, slice tests (`@DataJpaTest`, `@WebMvcTest`), security filter tests, and automated GitHub Actions CI.
- **Git & Workflows:** Branching strategy based on `developV2`, conventional commits, and Definition of Done.

Covered business modules: **Authentication**, **User / Profile**, **Community & Groups**, **Marketplace**, **Lost Item (Lost & Found)**, **Notifications**, and **Admin / Moderation**.

---

## 3. Architecture and Responsibilities

### 3.1 Backend Architecture

The backend strictly follows a layered architecture with clean boundary isolation:

```text
HTTP Request
     │
     ▼
[ Controller Layer ]      <── Request DTO validation (@Valid), route mapping
     │
     ▼
[ Service Layer ]         <── Business invariants, security context, throws semantic exceptions
     │
     ▼
[ Repository Layer ]      <── Spring Data JPA persistence interfaces
     │
     ▼
[ MySQL 8.4 Database ]
```

* **Controller Layer (`controller/`):**
  - Handles HTTP requests, parameter extraction, and request validation (`@Valid`).
  - Returns appropriate HTTP status codes wrapped in standard DTOs or `ApiResponse<T>`.
  - **Rules:** Must contain zero business logic; must never catch application exceptions; must never return raw JPA entities directly to the client.
* **Service Layer (`service/`):**
  - Enforces domain rules, checks resource ownership (IDOR prevention), and orchestrates transactions (`@Transactional`).
  - **Rules:** Must remain completely decoupled from HTTP classes (no `HttpServletRequest`, `ResponseEntity`, or `HttpStatus`). When rules fail, throws semantic application exceptions (`BadRequestException`, `ConflictException`, `ResourceNotFoundException`, `ForbiddenException`, `UnauthorizedException`).
* **Repository Layer (`repository/`):**
  - Extends `JpaRepository<Entity, ID>`.
  - Defines database queries using Spring Data method conventions or `@Query`.
* **Request DTOs (`dto/`):**
  - Java Records or POJOs defining the exact client payload accepted by the endpoint with Jakarta Bean Validation annotations.
* **Response DTOs (`dto/`):**
  - Explicit data transfer objects returned by endpoints.
* **Global Exception Handler (`common/exception/GlobalExceptionHandler.java`):**
  - Centralized `@RestControllerAdvice` mapping exceptions into canonical `ApiErrorResponse` instances.
* **Security Filter Chain (`config/SecurityConfig.java`):**
  - Stateless request filtering via `JwtAuthenticationFilter`.
  - Dedicated entry points serializing `ApiErrorResponse` for unauthenticated (`401`) and forbidden (`403`) requests.

### 3.2 Frontend Architecture

The frontend must:

- Follow the approved API specification under `/api/v1/...`.
- Maintain centralized API client instances (e.g., Axios or Fetch with interceptors for `Authorization: Bearer <token>`).
- Explicitly handle loading, success, empty, validation-error (`fields` mapping), and unauthorized states.
- Define TypeScript interfaces matching the backend response DTOs.
- Avoid treating client-side route guards or hidden buttons as security controls (the backend enforces all authorization).
- Cleanly clear local session state upon receiving HTTP `401 Unauthorized` or token expiration.

### 3.3 Database & Persistence

- Table and column names must strictly follow `snake_case` (e.g., `user_id`, `created_at`).
- Foreign key constraints, unique indexes (such as `users.email`), and check constraints must be declared at the database schema level.
- Entities must inherit or declare standardized audit timestamps (`createdAt`, `updatedAt`).

---

## 4. Client–Server JSON Contract

### 4.1 URL Path & Versioning Standard

- **Canonical Versioned Path:** All application endpoints must be exposed under `/api/v1/`.
  - Example: `POST /api/v1/auth/register`, `GET /api/v1/marketplace/listings`.
- **Backward Compatibility:** During active development, controllers and `SecurityConfig` may maintain aliases mapping both `/api/...` and `/api/v1/...`.
- **Case Convention:** URL paths must be lowercase, hyphen-separated kebab-case (e.g., `/api/v1/lost-found/items`).

### 4.2 Request JSON Format

Client request bodies must contain only accepted, validated fields:

```json
{
  "title": "Calculus 8th Edition",
  "description": "Mint condition with CD included",
  "price": 250000,
  "category": "BOOKS",
  "condition": "LIKE_NEW",
  "campusLocation": "Central Library"
}
```

Rules:
1. All input must be validated via Bean Validation (`@NotBlank`, `@NotNull`, `@Size`, `@Min`, `@Email`).
2. Client cannot supply or override server-managed attributes (such as `id`, `sellerId`, `createdAt`, `status`).
3. The authenticated user identity is extracted securely by the backend from the JWT `SecurityContextHolder`, never from a request body parameter.

### 4.3 Successful Response JSON

CampusHub adopts a standard envelope pattern for business domain resources, while keeping authentication tokens lightweight:

#### Standard Business Resource Envelope (`ApiResponse<T>`):
```json
{
  "data": {
    "id": "e9b1b7a2-1b1e-4b72-8f96-3e74b3d81b85",
    "title": "Calculus 8th Edition",
    "price": 250000,
    "category": "BOOKS",
    "status": "AVAILABLE",
    "createdAt": "2026-10-22T21:30:00"
  }
}
```

#### Authentication Endpoints Exception:
For `POST /api/v1/auth/register` and `POST /api/v1/auth/login`, endpoints return the direct `AuthResponse` contract:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### 4.4 Collection & Pagination Response

Paginated collection endpoints (Marketplace, Community feed, Lost & Found search) follow this unified structure:

```json
{
  "data": [
    {
      "id": "e9b1b7a2-1b1e-4b72-8f96-3e74b3d81b85",
      "title": "Calculus 8th Edition",
      "price": 250000,
      "category": "BOOKS",
      "status": "AVAILABLE"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

Rules:
- **Zero-based indexing:** `page` index starts at `0` (aligning with Spring Data `Pageable`).
- Default page size: `10` or `20` items; maximum page size enforced at backend: `100`.
- Sorting format: `sort=createdAt,desc` or `sort=price,asc`.

---

## 5. Standard Error Response Contract

### 5.1 Canonical Error Schema

All backend modules, controllers, security filters, and exception handlers strictly return the canonical [`ApiErrorResponse`](../backend/src/main/java/com/campushub/common/dto/ApiErrorResponse.java) schema:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fields": {
    "email": "Email format is invalid",
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2026-10-22T21:31:00"
}
```

For general resource or business errors without field-level breakdowns:

```json
{
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Marketplace listing not found",
  "fields": null,
  "timestamp": "2026-10-22T21:31:00"
}
```

### 5.2 Field Specifications

| Field | Type | Presence | Description |
|---|---|---|---|
| `status` | Integer | Required | HTTP status code (must strictly match HTTP response status) |
| `error` | String | Required | Machine-readable error code (e.g. `VALIDATION_ERROR`, `UNAUTHORIZED`, `CONFLICT`) |
| `message` | String | Required | Safe, human-readable summary message |
| `fields` | Object / Map | Optional | Key-value dictionary of invalid fields (`{"field": "error message"}`); `null` or omitted when not a validation failure |
| `timestamp` | String / DateTime | Required | ISO-8601 timestamp generated at time of failure |

> **Defense-in-Depth Rule:** The public API error contract deliberately **omits the `path` field** to eliminate endpoint and internal route enumeration. Diagnostic paths are retained solely in server-side SLF4J logs.

### 5.3 HTTP Status Code Conventions

| Status Code | Error Code Constant | Semantic Meaning & Trigger |
|---|---|---|
| `200 OK` | — | Successful request with response body |
| `201 Created` | — | Resource successfully created (`Location` header optional) |
| `204 No Content` | — | Successful operation with empty body (e.g., delete) |
| `400 Bad Request` | `BAD_REQUEST` / `VALIDATION_ERROR` | Malformed JSON syntax, invalid parameters, or Bean Validation failure |
| `401 Unauthorized` | `UNAUTHORIZED` | Missing, expired, or tampered JWT; bad login credentials |
| `403 Forbidden` | `FORBIDDEN` | Valid authentication but caller lacks role or does not own the resource |
| `404 Not Found` | `RESOURCE_NOT_FOUND` | Resource ID does not exist |
| `409 Conflict` | `CONFLICT` | State conflict: duplicate email, already-reserved item, concurrent order |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | Unexpected server failure; returns safe message `"An unexpected error occurred"` |

---

## 6. Security & Authorization Contract

1. **Stateless Request Authentication:** All protected endpoints require `Authorization: Bearer <jwt_token>`. Sessions are strictly stateless (`SessionCreationPolicy.STATELESS`).
2. **Server-Side Authorization:** The frontend never decides access permissions. Every mutation verifies:
   - **Role Check:** `@PreAuthorize("hasRole('ADMIN')")` or SecurityConfig rule.
   - **Ownership Check (IDOR Guard):** `resource.getAuthorId().equals(currentUser.getId())`. Non-owners receive `403 FORBIDDEN`.
3. **Password Security:**
   - Passwords must be hashed using BCrypt (`BCryptPasswordEncoder`).
   - Plaintext passwords must never be logged or stored.
4. **Information Hiding:**
   - SQL exceptions, stack traces, Hibernate query traces, and internal filesystem paths must **never** be exposed in API error responses.
   - Server-side logs capture detailed diagnostics via `log.error(...)` with sensitive credentials redacted.

---

## 7. Data, ID & Timestamp Conventions

### 7.1 Primary Key Strategy
- **UUIDs for Public Domain Entities:** Entities exposed to public routes and APIs (`User`, `Post`, `Listing`, `LostItem`, `Comment`, `Order`) use **UUID** (`java.util.UUID` in Java, `char(36)` or `binary(16)` in MySQL, and `String` in JSON).
- **Security Rationale:** Prevents sequential ID harvesting, enumeration attacks, and predictable resource URL guessing.

### 7.2 Audit Timestamps
- All database entities record:
  - `createdAt`: Set once upon creation (`updatable = false`).
  - `updatedAt`: Updated on every modification.
- API representations format timestamps as ISO-8601 (`YYYY-MM-DDTHH:mm:ss`).

---

## 8. Git, CI/CD and Branch Management

```mermaid
gitGraph
    commit id: "developV2-init"
    branch feature/DB-001
    checkout feature/DB-001
    commit id: "feat(db): user persistence"
    checkout developV2
    merge feature/DB-001 id: "merge PR"
    branch feature/SEC-002
    checkout feature/SEC-002
    commit id: "feat(sec): jwt auth"
    checkout developV2
    merge feature/SEC-002 id: "merge PR "
```

- **Integration Branch:** `developV2` is the active shared integration branch.
- **Feature Branches:** Created from `developV2` using naming convention:
  - `feature/<issue-code>-<short-description>` (e.g. `feature/SEC-002-security-authentication`)
  - `fix/<issue-code>-<short-description>`
- **Commit Messages:** Follow Conventional Commits: `feat(scope): ...`, `fix(scope): ...`, `test(scope): ...`, `docs: ...`.
- **CI Quality Gate:** All pull requests targeting `developV2` must pass automated GitHub Actions (`.github/workflows/ci.yml`):
  - Frontend: Node 24 `npm run lint` & `npm run build`.
  - Backend: Java 21 Temurin `mvn verify` against MySQL 8.4 service container.

---

## 9. Definition of Ready (DoR) & Definition of Done (DoD)

### Definition of Ready (DoR)
A task or module is ready for development only when:
1. Endpoint URLs, methods, request schemas, and response DTOs are documented.
2. Authorization and ownership boundaries are identified.
3. Database entity attributes, primary key types, and constraints are defined.

### Definition of Done (DoD)
A task or pull request is complete only when:
- [ ] Code compiles cleanly without compiler warnings.
- [ ] Endpoints adhere to canonical `/api/v1/` routing.
- [ ] Success responses use agreed DTOs or `ApiResponse<T>` envelope.
- [ ] Error responses strictly use `ApiErrorResponse` (`status`, `error`, `message`, `fields`, `timestamp`).
- [ ] Server-side input validation (`@Valid`) and ownership checks are enforced.
- [ ] Zero stack traces, SQL internals, or secrets leaked in responses or Git history.
- [ ] Automated tests execute with 100% green pass locally (`./mvnw test` = 0 failures).
- [ ] GitHub Actions CI pipeline passes cleanly.
- [ ] PR is reviewed and approved before merging into `developV2`.

---

## 10. Approval & Verification Record

| Role | Name / Assignee | Responsibility | Status |
|---|---|---|:---:|
| **Technical Lead** | Project Lead | Overall Architecture & Contract Governance | ✅ Approved |
| **Member 1 (Database)** | Member 1 | MySQL Persistence, JPA & User Entity | ✅ Approved |
| **Member 2 (Security)** | Member 2 | Spring Security, Stateless Filter Chain & JWT | ✅ Approved |
| **Member 3 (API Foundation)** | Member 3 | Global Errors, Validation & Conventions | ✅ Approved |
| **Member 4 (Testing & CI)** | Member 4 | MockMvc Tests, Quality Gates & CI Pipeline | ✅ Approved |

*Document Effective Date: October 23, 2026*  
*Canonical Reference: [`docs/DESIGN_CONTRACT.md`](DESIGN_CONTRACT.md)*

