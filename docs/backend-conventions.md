# CampusHub — Backend Architecture & Development Conventions

This guide establishes the backend development standards, architectural layer responsibilities, error-handling contracts, and conventions for all future modules (Profile, Community, Marketplace, Lost & Found, etc.).

---

## 1. Package Structure Convention

All module features must adhere to a standardized layered architecture. Modules may be organized either by layer or by feature package containing these standard sub-packages:

```text
com.campushub
├── common/                     # Cross-cutting, shared foundation
│   ├── dto/                    # Standard API response contracts (ApiErrorResponse)
│   ├── entity/                 # Reusable base entities (BaseEntity)
│   └── exception/              # Global handler & reusable application exceptions
├── config/                     # Spring configurations (JpaConfig, WebMvcConfig, etc.)
└── <module>/                   # Business modules (e.g., auth, profile, marketplace)
    ├── controller/             # HTTP endpoints and routing
    ├── service/                # Business logic and transaction orchestration
    │   └── impl/               # Service implementations (if using interfaces)
    ├── repository/             # Spring Data JPA interfaces
    ├── entity/                 # Database entity mappings
    ├── dto/                    # Request/Response contracts (Record/POJO)
    └── exception/              # Module-specific exceptions (if any)
```

---

## 2. Layer Responsibilities & Boundaries

### A. Controller Layer (`controller/`)
* **Role:** Handle HTTP requests, URL routing, request parameter/body validation (`@Valid`), and return standard HTTP status codes (`ResponseEntity`).
* **Rules:**
  * **No business logic:** Controllers must never implement business rules, calculations, or direct database queries.
  * **Do not catch application exceptions:** Let semantic exceptions bubble up to `GlobalExceptionHandler`.
  * **Always return DTOs:** Never return raw JPA entities directly to the client.

### B. Service Layer (`service/`)
* **Role:** Business rule enforcement, validation, orchestrating repositories, transaction boundaries (`@Transactional`).
* **Rules:**
  * **Free of HTTP details:** Services must not import `HttpServletRequest`, `ResponseEntity`, or `HttpStatus`.
  * **Throw semantic exceptions:** When a business rule fails, throw standard application exceptions (e.g., `ResourceNotFoundException`, `ConflictException`, `BadRequestException`).

### C. Repository Layer (`repository/`)
* **Role:** Persistence and database queries via Spring Data JPA.
* **Rules:**
  * Extend `JpaRepository<Entity, ID>`.
  * Define query methods using Spring Data conventions or `@Query` with JPQL/native SQL.
  * Never place business validations in the repository layer.

### D. Entity Layer (`entity/`)
* **Role:** Object-Relational Mapping (ORM) representing database tables.
* **Rules:**
  * Extend [`BaseEntity`](CampusHub/backend/src/main/java/com/campushub/common/entity/BaseEntity.java) to automatically benefit from `createdAt` and `updatedAt` audit timestamps.
  * Use snake_case for table and column names (`@Table(name = "user_profiles")`, `@Column(name = "phone_number")`).
  * Never expose JPA entities directly in controller request/response signatures.

### E. DTO Layer (`dto/`)
* **Role:** Define explicit API contracts for incoming requests (`CreateXRequest`, `UpdateXRequest`) and outgoing responses (`XResponse`).
* **Rules:**
  * Annotate request DTOs with Jakarta Bean Validation constraints (`@NotBlank`, `@NotNull`, `@Email`, `@Size`, etc.).
  * Only expose fields intended for client consumption.

---

## 3. Data & Error Flow

### Standard Request Flow
```text
Client (HTTP Request)
        ↓
Controller (@Valid RequestDTO)
        ↓
Service (Business Logic & Orchestration)
        ↓
Repository (JPA Queries)
        ↓
Database (PostgreSQL / MySQL)
```

### Centralized Error Flow
```text
Service / Controller throws Exception
        ↓
GlobalExceptionHandler (@RestControllerAdvice)
        ↓
Mapped to ApiErrorResponse DTO
        ↓
HTTP Response (Status Line + JSON Payload)
```

---

## 4. Standard API Error Contract

All error responses across every module **must strictly use** the shared [`ApiErrorResponse`](CampusHub/backend/src/main/java/com/campushub/common/dto/ApiErrorResponse.java) schema. Modules must never invent custom error response formats.

### Error Response Schema
```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fields": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2026-10-06T12:00:00"
}
```

### HTTP Status & Exception Mapping Table

| HTTP Status | Error Code | Common Exception | When to Use |
| :--- | :--- | :--- | :--- |
| **`400 Bad Request`** | `VALIDATION_ERROR` | `MethodArgumentNotValidException` | Request body failed Bean Validation (`@Valid`) |
| **`400 Bad Request`** | `BAD_REQUEST` | `BadRequestException` | Malformed input, invalid query parameter |
| **`401 Unauthorized`** | `UNAUTHORIZED` | `UnauthorizedException` | Missing, expired, or invalid authentication token |
| **`403 Forbidden`** | `FORBIDDEN` | `ForbiddenException` | Authenticated user lacks permission for the resource |
| **`404 Not Found`** | `RESOURCE_NOT_FOUND` | `ResourceNotFoundException` | Resource with given ID does not exist in the database |
| **`409 Conflict`** | `CONFLICT` | `ConflictException` | Business conflict (duplicate email, already reserved slot) |
| **`500 Internal Server Error`** | `INTERNAL_SERVER_ERROR` | `Exception` (catch-all) | Unexpected runtime bugs, unhandled exceptions |

---

## 5. Security & Information Safety Rules

1. **No Stack Trace Leakage:** Stack traces must never be sent in API responses to clients.
2. **No Database / SQL Exposure:** Never leak raw database error messages, table names, constraint names, or SQL syntax to the client on `500` errors.
3. **Safe 500 Responses:** Generic server errors must always return:
   ```json
   {
     "status": 500,
     "error": "INTERNAL_SERVER_ERROR",
     "message": "An unexpected error occurred"
   }
   ```
   Original exception details must be logged internally using SLF4J (`log.error(...)`).

---

## 6. Audit Timestamps (`BaseEntity`)

Every newly created persistent entity should inherit from [`BaseEntity`](CampusHub/backend/src/main/java/com/campushub/common/entity/BaseEntity.java):

```java
@Entity
@Table(name = "community_posts")
public class CommunityPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // other fields...
}
```

* **`createdAt`**: Automatically populated with current UTC/server timestamp on initial save (`INSERT`). Marked `updatable = false` to prevent accidental overwrites.
* **`updatedAt`**: Automatically updated on every save/update operation (`UPDATE`).
* JPA Auditing is enabled globally via [`JpaConfig`](CampusHub/backend/src/main/java/com/campushub/config/JpaConfig.java).

---

## 7. Testing Conventions & Verification

All new backend features and modules must include automated tests before merging to `develop`.

### A. Testing Conventions by Layer

| Layer | Testing Strategy | Recommended Tools |
| :--- | :--- | :--- |
| **Controller / API** | Verify HTTP status codes, headers, and JSON structure with `MockMvc`. | `MockMvc`, `@WebMvcTest` or `MockMvcBuilders.standaloneSetup()` |
| **Service** | Test business logic, branch conditions, and exception throwing. Mock out repositories. | JUnit 5, Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`) |
| **Repository & Entity** | Test database mapping, queries, and constraints against an in-memory/test DB. | `@DataJpaTest`, `TestEntityManager` |
| **Common / DTO / Utils** | Test serialization, static factory methods, and helper methods. | Plain JUnit 5 + AssertJ (`assertThat`) |

### B. Standard Test Commands

Execute tests inside the `backend` directory using the Maven Wrapper:

```bash
# Run the entire test suite
cd backend
./mvnw test

# Run a specific test class
./mvnw test -Dtest=GlobalExceptionHandlerTest

# Run a specific test method
./mvnw test -Dtest=GlobalExceptionHandlerTest#shouldHandleValidationErrors
```

### C. Foundation Test Suite Status

The shared backend foundation test suite is fully verified and passing:

| Test Class | Scope Covered | Status |
| :--- | :--- | :---: |
| [`GlobalExceptionHandlerTest`](CampusHub/backend/src/test/java/com/campushub/common/exception/GlobalExceptionHandlerTest.java) | 400 Bad Request, 400 Validation Errors, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 500 Safe Handling (No stack traces leaked) | ✅ **PASS** |
| [`ApiErrorResponseTest`](CampusHub/backend/src/test/java/com/campushub/common/dto/ApiErrorResponseTest.java) | DTO constructors, static factory methods (`.of()`), timestamp generation, field mappings | ✅ **PASS** |
| [`BaseEntityTest`](CampusHub/backend/src/test/java/com/campushub/common/entity/BaseEntityTest.java) | JPA Auditing `@CreatedDate` on INSERT and `@LastModifiedDate` on UPDATE | ✅ **PASS** |
| [`BackendApplicationTests`](CampusHub/backend/src/test/java/com/campushub/BackendApplicationTests.java) | Spring ApplicationContext loads cleanly | ✅ **PASS** |
| **Total Test Suite** | **13 tests run, 0 failures, 0 errors, 0 skipped** | ✅ **BUILD SUCCESS** |

