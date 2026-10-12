# [ISSUE-04] Test Suite Reconciliation, Security MockMvc Tests & CI Pipeline Acceptance

## Objective
Establish the automated testing gate, resolve existing unit test breakages, implement dedicated Spring Security filter tests, and coordinate the Phase D Acceptance Testing across all members before major business modules are implemented.

This task unblocks the CI build, verifies the decoupled security error responses in MockMvc, and validates the Definition of Done across the entire foundation phase.

---

## Scope
Implement the quality and verification foundation for:
- Immediate reconciliation of the 2 failing tests in `GlobalExceptionHandlerTest`
- Spring Security filter-chain testing (`SecurityErrorHandlingTest`) using MockMvc
- GitHub Actions CI workflow monitoring (`ci.yml` with MySQL 8.4 container)
- User persistence and registration test scenarios
- Phase D Acceptance Testing coordination and Definition of Done verification

---

## Requirements

### Immediate Blocker: Reconcile GlobalExceptionHandlerTest
- File: [`backend/src/test/java/com/campushub/common/exception/GlobalExceptionHandlerTest.java`](../../backend/src/test/java/com/campushub/common/exception/GlobalExceptionHandlerTest.java).
- **Issue:** Lines 68 and 78 test `throwUnauthorized()` and `throwForbidden()`, expecting 401 and 403 from standalone MockMvc. However, those exceptions were removed from `@RestControllerAdvice`, causing tests to fail with 500.
- **Action:**
  - Remove `shouldHandleUnauthorizedException` and `shouldHandleForbiddenException` from `GlobalExceptionHandlerTest`.
  - Remove `throwUnauthorized()` and `throwForbidden()` from `TestDummyController`.
  - Verify that `GlobalExceptionHandlerTest` tests only MVC-level exceptions (400 Bad Request, 400 Validation, 404 Not Found, 409 Conflict, 500 Unexpected).

### Security Filter-Chain Testing (`SecurityErrorHandlingTest`)
- Create `src/test/java/com/campushub/security/SecurityErrorHandlingTest.java`:
  - Annotate with `@SpringBootTest` and `@AutoConfigureMockMvc`.
  - Test 1: `shouldReturn401WhenAccessingProtectedEndpointWithoutAuthentication`:
    - Perform `get("/api/admin/dashboard")` without credentials.
    - Expect HTTP status `401 Unauthorized`.
    - Expect JSON body: `$.status = 401`, `$.error = "UNAUTHORIZED"`, `$.message = "Authentication is required to access this resource"`, `$.timestamp` exists.
  - Test 2: `shouldReturn403WhenAccessingAdminEndpointWithUserRole`:
    - Perform `get("/api/admin/dashboard")` with `@WithMockUser(roles = "USER")`.
    - Expect HTTP status `403 Forbidden`.
    - Expect JSON body: `$.status = 403`, `$.error = "FORBIDDEN"`, `$.message = "You do not have permission to access this resource"`, `$.timestamp` exists.
  - Test 3: `shouldAllowAccessToPublicHealthEndpoint`:
    - Perform `get("/api/health")` without credentials.
    - Expect HTTP status `200 OK`.

### GitHub Actions CI Pipeline Verification
- File: [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml).
- Verify both workflow jobs pass on Ubuntu:
  - `frontend`: Node 24, `npm ci`, `npm run lint`, `npm run build`.
  - `backend`: Java 21 Temurin, MySQL 8.4 service container, `mvn --batch-mode verify`.
- Ensure tests run cleanly without flaky failures.

### Persistence & Auth Integration Scenarios
- Support Member 1 in verifying `UserRepositoryTest` with `@DataJpaTest`.
- Prepare test cases for Member 2's auth endpoints:
  - Valid registration $\rightarrow$ 200/201.
  - Duplicate email registration $\rightarrow$ 409 Conflict.
  - Invalid registration payload (blank email) $\rightarrow$ 400 Validation Error.
  - Login with incorrect password $\rightarrow$ 401 Unauthorized.

### Phase D Acceptance Testing & Quality Gate
- Run full test suite:
  ```bash
  cd backend
  ./mvnw clean test
  ```
- Verify **zero test failures, zero errors, zero skipped**.
- Review team PRs against the Definition of Done.
- Report any defects with clear reproduction steps, HTTP status codes, and JSON response diffs.

---

## Notes
- Report reproducible steps, request payloads, and stack traces rather than simply reporting a red CI build.
- Maintain test independence; tests must not depend on database state left by preceding tests.

---

## Deliverables
- Reconciled `GlobalExceptionHandlerTest.java` (passing 100%).
- `SecurityErrorHandlingTest.java` verifying 401, 403, and public endpoints.
- Full local test suite report demonstrating 100% green build.
- Acceptance testing checklist sign-off for Phase D.
- PR targeting `developV2`.

---

## Definition of Done
- [ ] `GlobalExceptionHandlerTest` passes with 0 failures.
- [ ] `SecurityErrorHandlingTest` passes and asserts standard `ApiErrorResponse` for 401 and 403.
- [ ] Public endpoint `/api/health` passes without authentication.
- [ ] Full backend test suite passes locally (`./mvnw test` = 0 failures).
- [ ] GitHub Actions CI pipeline passes cleanly on pull request.
- [ ] PR created and reviewed against `developV2`.

---

## Suggested Branch
`feature/TEST-001-testing-integration-ci-foundation`

## Suggested Commit
`test(backend): reconcile global exception tests and add security filter tests`

