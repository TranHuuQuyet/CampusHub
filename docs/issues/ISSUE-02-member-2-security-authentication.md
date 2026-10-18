# [ISSUE-02] Spring Security, Stateless Filter Chain & JWT Authentication Foundation

## Objective
Establish the application security architecture, stateless request filtering, password hashing, and unified 401/403 security error handling before business modules such as Profile, Community, Marketplace, and Lost & Found are implemented.

This task integrates custom security handlers with the shared API error model, establishes the stateless JWT engine, and prepares authentication endpoints.

---

## Scope
Implement the reusable security foundation for:
- Refactored `SecurityConfig` with stateless session management and route protection
- Dedicated 401 (`RestAuthenticationEntryPoint`) and 403 (`RestAccessDeniedHandler`) handlers writing the canonical `ApiErrorResponse`
- BCrypt `PasswordEncoder` bean
- Stateless JWT engine (`JwtService` and `JwtAuthenticationFilter`)
- Authentication endpoints (`/api/auth/register`, `/api/auth/login`) and request/response DTOs

---

## Requirements

### Security Configuration (`SecurityConfig`)
- Class name must strictly follow PascalCase convention: [`SecurityConfig.java`](../../backend/src/main/java/com/campushub/config/securityConfig.java) (rename existing `securityConfig.java`).
- Annotate with `@Configuration` and `@EnableWebSecurity`.
- Configure `SecurityFilterChain`:
  - Disable CSRF: `csrf -> csrf.disable()` (appropriate for stateless REST API with Bearer tokens).
  - Session policy: `SessionCreationPolicy.STATELESS`.
  - Authorize HTTP requests:
    - `.requestMatchers("/api/auth/**").permitAll()`
    - `.requestMatchers("/api/health").permitAll()`
    - `.requestMatchers("/api/admin/**").hasRole("ADMIN")`
    - `.anyRequest().authenticated()`
  - Exception handling: register `RestAuthenticationEntryPoint` and `RestAccessDeniedHandler`.

### 401 Unauthorized Handler (`RestAuthenticationEntryPoint`)
- Implements Spring Security's `AuthenticationEntryPoint`.
- Writes HTTP 401 status directly to `HttpServletResponse`.
- Serializes canonical [`ApiErrorResponse`](../../backend/src/main/java/com/campushub/common/dto/ApiErrorResponse.java) using Jackson 3 (`tools.jackson.databind.ObjectMapper`).
- Payload: `ApiErrorResponse.of(401, "UNAUTHORIZED", "Authentication is required to access this resource")`.
- Remove unused imports (`PrintWriter`, `javax.print.attribute.standard.Media`).

### 403 Forbidden Handler (`RestAccessDeniedHandler`)
- Implements Spring Security's `AccessDeniedHandler`.
- Writes HTTP 403 status directly to `HttpServletResponse`.
- Serializes canonical [`ApiErrorResponse`](../../backend/src/main/java/com/campushub/common/dto/ApiErrorResponse.java) using `tools.jackson.databind.ObjectMapper`.
- Payload: `ApiErrorResponse.of(403, "FORBIDDEN", "You do not have permission to access this resource")`.

### Password Hashing
- Declare `@Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }` inside `SecurityConfig`.
- Plaintext passwords must never be stored or evaluated.

### JWT Engine (`JwtService`)
- Add JJWT dependencies to `backend/pom.xml` (`io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson`, version `0.12.x`).
- Package: `com.campushub.security.jwt.JwtService`.
- Implement methods:
  - `String generateToken(String username, Map<String, Object> extraClaims)`
  - `String extractUsername(String token)`
  - `boolean isTokenValid(String token, String username)`
  - `boolean isTokenExpired(String token)`
- Externalize JWT secret and expiration time in `application.properties`:
  - `jwt.secret=${JWT_SECRET:defaultSecretKeyForDevelopmentOnlyPleaseChangeInProduction}`
  - `jwt.expiration-ms=86400000` (24 hours)

### JWT Filter (`JwtAuthenticationFilter`)
- Package: `com.campushub.security.jwt.JwtAuthenticationFilter` extending `OncePerRequestFilter`.
- Intercept incoming requests, parse `Authorization: Bearer <token>` header.
- Validate token with `JwtService`, load user details, and set `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`.
- Register filter in `SecurityConfig`: `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`.

### Authentication Endpoints
- DTOs: `RegisterRequest`, `LoginRequest`, `AuthResponse` (`token`, `tokenType`, `expiresIn`).
- Controller: `AuthController` exposing:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
- Integrate with Member 1's `UserRepository`.

### Security / Error Safety
- Zero secret keys or passwords committed to Git.
- Never leak internal authentication exceptions or stack traces to clients.
- Reuse Member 3's shared [`ApiErrorResponse`](../../backend/src/main/java/com/campushub/common/dto/ApiErrorResponse.java); do not create a competing error response DTO.

### Backend Conventions
- Package structure:
  - `com.campushub.config`
  - `com.campushub.common.security`
  - `com.campushub.security.jwt`
  - `com.campushub.auth` (or `com.campushub.user.controller`)
- Handlers annotated with `@Component`.

### Testing
- Unit test `JwtServiceTest` verifying token creation, claim parsing, and expiration validation.
- Coordinate with Member 4 on `SecurityErrorHandlingTest` for MockMvc filter verification.

---

## Notes
- Do not perform broad refactoring of business modules.
- Coordinate the `User` entity and repository dependency with Member 1.

---

## Deliverables
- Renamed and polished `SecurityConfig.java`.
- Cleaned [`RestAuthenticationEntryPoint.java`](../../backend/src/main/java/com/campushub/common/security/RestAuthenticationEntryPoint.java) and [`RestAccessDeniedHandler.java`](../../backend/src/main/java/com/campushub/common/security/RestAccessDeniedHandler.java).
- JJWT dependencies in `pom.xml`.
- `JwtService.java` and `JwtAuthenticationFilter.java`.
- `AuthController.java` and `AuthService.java`.
- PR targeting `developV2`.

---

## Definition of Done
- [x] `SecurityConfig` compiles with stateless filter chain and custom 401/403 handlers.
- [x] Unauthenticated requests receive HTTP 401 with standard `ApiErrorResponse`.
- [x] Unauthorized requests receive HTTP 403 with standard `ApiErrorResponse`.
- [ ] Passwords stored using BCrypt hash.
- [ ] Valid JWT permits access; invalid or expired token is rejected.
- [ ] Full Maven build passes cleanly (`./mvnw test`).
- [ ] PR created and reviewed against `developV2`.

---

## Suggested Branch
`feature/SEC-001-security-jwt-foundation`

## Suggested Commit
`feat(security): configure stateless security filter chain and jwt authentication`

