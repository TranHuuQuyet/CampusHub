# CampusHub — Foundation Phase: Present Progress & Status

> **Date:** October 10, 2026  
> **Active Branch:** `developV2`  
> **Source Repository:** `CampusHub` (Backend module)  
> **Environment:** Java 21, Spring Boot 4.1.1, MySQL 8.4, Maven Wrapper (`./mvnw`)

---

## 1. Overall Team Progress Dashboard

```
┌─────────────────────────────────────────────────────────────┐
│                 CAMPUSHUB FOUNDATION PHASE                  │
│                      Overall: ~60%                          │
├──────────────────────────────┬──────────────────────────────┤
│ Member 1 (Database)          │ [█████░░░░░] 50% In Progress │
│ Member 2 (Security - You)    │ [██████░░░░] 60% In Progress │
│ Member 3 (Common Errors)     │ [████████░░] 85% Near Done   │
│ Member 4 (Testing & CI)      │ [████░░░░░░] 40% Action Req. │
└──────────────────────────────┴──────────────────────────────┘
```

---

## 2. Current Build & Test Health

| Check | Command | Status | Details |
| :--- | :--- | :---: | :--- |
| **Java Compilation** | `./mvnw compile` | ✅ **PASS** | Source code compiles cleanly without errors. |
| **Automated Tests** | `./mvnw test` | ❌ **FAIL** | **13 run, 11 passed, 2 failed, 0 errors, 0 skipped.** |
| **GitHub Actions CI** | `.github/workflows/ci.yml` | 🟢 **Ready** | Configured for Node 24 lint/build & Java 21 `mvn verify` with MySQL 8.4 container. |

### 🔍 Failing Tests Breakdown
```text
[ERROR] Failures:
[ERROR]   GlobalExceptionHandlerTest.shouldHandleUnauthorizedException:68 Status expected:<401> but was:<500>
[ERROR]   GlobalExceptionHandlerTest.shouldHandleForbiddenException:78 Status expected:<403> but was:<500>
```

#### Root Cause Analysis:
1. `UnauthorizedException` and `ForbiddenException` were deleted from the project, and their `@ExceptionHandler` methods were removed from [`GlobalExceptionHandler.java`](backend/src/main/java/com/campushub/common/exception/GlobalExceptionHandler.java).
2. The standalone test in [`GlobalExceptionHandlerTest.java`](backend/src/test/java/com/campushub/common/exception/GlobalExceptionHandlerTest.java) still had a dummy controller throwing those exceptions.
3. Without specific handlers in `@RestControllerAdvice`, the exceptions fell through to `handleUnexpectedException(Exception.class)`, returning HTTP `500` instead of `401`/`403`.
4. In Spring Security, HTTP 401 and 403 errors are intercepted at the **Servlet Filter Chain** before reaching `@RestControllerAdvice`. The test suite must be updated to test filter-level security separately from controller advice.

---

## 3. Present Progress by Member

### 👤 Member 1 — Database & Persistence
**Current Completion:** ~50%  
**Status:** In Progress (Infrastructure ready, entity pending)

- [x] **MySQL Dependencies:** `mysql-connector-j` and `spring-boot-starter-data-jpa` present in `backend/pom.xml`.
- [x] **DataSource Properties:** Configured in [`application.properties`](backend/src/main/resources/application.properties) with `${DB_USERNAME}` and `${DB_PASSWORD}`.
- [x] **JPA Auditing:** [`BaseEntity.java`](backend/src/main/java/com/campushub/common/entity/BaseEntity.java) implemented with `@CreatedDate` and `@LastModifiedDate`; enabled via [`JpaConfig.java`](backend/src/main/java/com/campushub/config/JpaConfig.java).
- [x] **Audit Unit Tests:** [`BaseEntityTest.java`](backend/src/test/java/com/campushub/common/entity/BaseEntityTest.java) passes cleanly.
- [ ] **Pending: User Entity & Repository:** `User.java` and `UserRepository.java` are not yet created.
- [ ] **Pending: Database Migrations:** Schema currently relies on `hibernate.ddl-auto=update`.

---

### 👤 Member 2 — Security & Authentication (You)
**Current Completion:** ~60%  
**Status:** In Progress (Filter chain & handlers ready; class rename, cleanup & JWT pending)

- [x] **Security Starter:** `spring-boot-starter-security` added to `backend/pom.xml`.
- [x] **Security Filter Chain:** Configured in [`securityConfig.java`](backend/src/main/java/com/campushub/config/securityConfig.java) with stateless session policy and path protection (`/api/auth/**`, `/api/health`, `/api/admin/**`).
- [x] **Authentication Entry Point (401):** Implemented in [`RestAuthenticationEntryPoint.java`](backend/src/main/java/com/campushub/common/security/RestAuthenticationEntryPoint.java) serializing `ApiErrorResponse` (`UNAUTHORIZED`).
- [x] **Access Denied Handler (403):** Implemented in [`RestAccessDeniedHandler.java`](backend/src/main/java/com/campushub/common/security/RestAccessDeniedHandler.java) serializing `ApiErrorResponse` (`FORBIDDEN`).
- [x] **Jackson 3 Integration:** Uses `tools.jackson.databind.ObjectMapper` matching Spring Boot 4.
- [ ] **Pending Fix: Class Naming:** Class name is `securityConfig` (lowercase). Needs renaming to `SecurityConfig.java`.
- [ ] **Pending Fix: Unused Imports:** Remove unused `PrintWriter` and `javax.print...Media` from `RestAuthenticationEntryPoint.java`.
- [ ] **Pending: JWT Dependencies & Service:** JJWT library not yet added to `pom.xml`; `JwtService` and `JwtAuthenticationFilter` not yet implemented.
- [ ] **Pending: PasswordEncoder Bean:** `BCryptPasswordEncoder` bean not yet declared in `SecurityConfig`.
- [ ] **Pending: Auth Controller & Service:** Registration and login endpoints waiting for Member 1's `UserRepository`.

---

### 👤 Member 3 — Common API Error Foundation
**Current Completion:** ~85%  
**Status:** Near Complete (Core handlers working; cleanup & test alignment pending)

- [x] **Canonical Error DTO:** [`ApiErrorResponse.java`](backend/src/main/java/com/campushub/common/dto/ApiErrorResponse.java) implemented with static factories and field-level error mapping.
- [x] **DTO Tests:** [`ApiErrorResponseTest.java`](backend/src/test/java/com/campushub/common/dto/ApiErrorResponseTest.java) passes (4 tests).
- [x] **Application Exceptions:** [`BadRequestException`](backend/src/main/java/com/campushub/common/exception/BadRequestException.java), [`ConflictException`](backend/src/main/java/com/campushub/common/exception/ConflictException.java), [`ResourceNotFoundException`](backend/src/main/java/com/campushub/common/exception/ResourceNotFoundException.java).
- [x] **Centralized Exception Handler:** [`GlobalExceptionHandler.java`](backend/src/main/java/com/campushub/common/exception/GlobalExceptionHandler.java) handles 400, 404, 409, Bean Validation mapping (`MethodArgumentNotValidException`), and 500 safe information hiding.
- [x] **Architecture Conventions:** [`docs/backend-conventions.md`](docs/backend-conventions.md) is written and committed.
- [ ] **Pending Fix: Duplicate Class Cleanup:** Untracked file [`backend/src/main/java/com/campushub/common/exception/AccessDeniedException.java`](backend/src/main/java/com/campushub/common/exception/AccessDeniedException.java) should be removed to avoid conflicting with Spring Security's native class.
- [ ] **Pending Coordination:** Align with Member 4 on separating controller-advice unit tests from filter-chain security tests.

---

### 👤 Member 4 — Testing, Integration & CI
**Current Completion:** ~40%  
**Status:** Action Required (CI ready; 2 failing unit tests need immediate resolution)

- [x] **CI Pipeline:** [`.github/workflows/ci.yml`](.github/workflows/ci.yml) configured with MySQL container and build steps.
- [x] **Passing Foundation Tests:**
  - `ApiErrorResponseTest`: ✅ 4/4 PASS
  - `BaseEntityTest`: ✅ 1/1 PASS
  - `BackendApplicationTests`: ✅ 1/1 PASS
- [ ] **Failing Tests:** [`GlobalExceptionHandlerTest.java`](backend/src/test/java/com/campushub/common/exception/GlobalExceptionHandlerTest.java) (2 failures).
- [ ] **Pending: Security Filter Tests:** Need `SecurityErrorHandlingTest.java` verifying 401 unauthenticated and 403 forbidden responses against Spring Security.
- [ ] **Pending: User Persistence Tests:** `UserRepositoryTest` waiting for Member 1's entity implementation.

---

## 4. Immediate Action Items & Blocker Resolution

```
Action Sequence:
1. Member 4 & 3: Reconcile GlobalExceptionHandlerTest ──> Unblock ./mvnw test (100% Green)
2. Member 2: Rename securityConfig -> SecurityConfig & clean unused imports
3. Member 4: Implement SecurityErrorHandlingTest (MockMvc verifying 401/403)
4. Member 1: Create User entity & UserRepository
5. Member 2: Add JWT dependencies & implement JwtService / JwtAuthenticationFilter
```

| Priority | Task | Owner | Target File |
| :---: | :--- | :---: | :--- |
| **P1** | Remove deleted exception tests from `GlobalExceptionHandlerTest` | Member 4 | [`backend/src/test/java/com/campushub/common/exception/GlobalExceptionHandlerTest.java`](backend/src/test/java/com/campushub/common/exception/GlobalExceptionHandlerTest.java) |
| **P1** | Delete redundant custom `AccessDeniedException.java` | Member 3 | [`backend/src/main/java/com/campushub/common/exception/AccessDeniedException.java`](backend/src/main/java/com/campushub/common/exception/AccessDeniedException.java) |
| **P2** | Rename `securityConfig.java` $\rightarrow$ `SecurityConfig.java` | Member 2 | [`backend/src/main/java/com/campushub/config/securityConfig.java`](backend/src/main/java/com/campushub/config/securityConfig.java) |
| **P2** | Remove unused imports from `RestAuthenticationEntryPoint.java` | Member 2 | [`backend/src/main/java/com/campushub/common/security/RestAuthenticationEntryPoint.java`](backend/src/main/java/com/campushub/common/security/RestAuthenticationEntryPoint.java) |
| **P3** | Create `SecurityErrorHandlingTest.java` for filter-level 401/403 | Member 4 | `backend/src/test/java/com/campushub/security/SecurityErrorHandlingTest.java` |
| **P4** | Implement `User.java` and `UserRepository.java` | Member 1 | `backend/src/main/java/com/campushub/user/` |
| **P4** | Implement JWT authentication layer | Member 2 | `backend/src/main/java/com/campushub/security/jwt/` |

