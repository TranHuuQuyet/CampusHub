# CampusHub Constitution & Engineering Guidelines

1. **NO VIBE CODING:** Never jump straight to writing code in `src/`. Always verify that an approved specification exists in `.specify/tasks/` first.
2. **DETERMINISTIC OUTPUTS:** Use explicit types, data models, and schemas (Java 21 records, typed DTOs, JPA entities). Avoid unstructured string parsing.
3. **QUALITY GATES:** All code written must directly map to a predefined requirement and Acceptance Criteria (AC) in the task specification. All slice and unit tests must pass.
4. **TECH STACK & ARCHITECTURE:**
   - **Backend:** Java 21, Spring Boot 4.x, Spring Data JPA, Hibernate, Jackson 3.
   - **Database:** MySQL 8.4 (InnoDB, `utf8mb4_unicode_ci`, UTC timezone, snake_case schema).
   - **Layer Isolation:** Controller -> Service -> Repository -> Database. Entities must never leak directly into HTTP controller responses.
   - **Security Rules:**
     - Authentication passwords: One-way salted hashing strictly using BCrypt.
     - Sensitive PII at rest: Two-way authenticated symmetric encryption (AES-256-GCM) with Base64 payload.
     - Public domain identifiers: UUIDv4.
