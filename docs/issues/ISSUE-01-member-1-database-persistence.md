# [ISSUE-01] MySQL Persistence, Spring Data JPA & User Entity Foundation

## Objective
Establish the persistent relational data layer for CampusHub using MySQL 8.4 and Spring Data JPA / Hibernate before business modules such as Profile, Community, Marketplace, and Lost & Found are implemented.

This task establishes the database connection, JPA Auditing superclass (`BaseEntity`), schema conventions, and the initial `User` entity contract required by authentication.

---

## Scope
Implement the reusable persistence foundation for:
- Secure MySQL connection configured via environment variables
- Spring Data JPA and Hibernate settings
- Audit timestamp foundation (`BaseEntity` with `@CreatedDate` & `@LastModifiedDate`)
- User Entity and Repository contract (`User`, `UserRepository`)
- Persistence testing using Spring Boot slice tests (`@DataJpaTest`)

---

## Requirements

### Database Connection & Environment Variables
- Externalize database credentials in [`application.properties`](../../backend/src/main/resources/application.properties) via `${DB_USERNAME}` and `${DB_PASSWORD}`.
- Configure connection URL: `jdbc:mysql://localhost:3306/campushub?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`.
- Never commit hardcoded database passwords or production secrets to Git.

### JPA & Hibernate Configuration
- Set `spring.jpa.hibernate.ddl-auto=update` for local development.
- Configure `spring.jpa.show-sql=true` and `spring.jpa.properties.hibernate.format_sql=true` for local debug visibility.
- Set `spring.jpa.open-in-view=false` to prevent open-session-in-view anti-patterns.
- Verify MySQL JDBC driver (`mysql-connector-j`) and `spring-boot-starter-data-jpa` in `backend/pom.xml`.

### Audit Timestamp Foundation (`BaseEntity`)
- Implement [`BaseEntity.java`](../../backend/src/main/java/com/campushub/common/entity/BaseEntity.java) annotated with `@MappedSuperclass` and `@EntityListeners(AuditingEntityListener.class)`.
- Fields:
  - `createdAt`: `LocalDateTime`, annotated with `@CreatedDate`, `@Column(name = "created_at", nullable = false, updatable = false)`.
  - `updatedAt`: `LocalDateTime`, annotated with `@LastModifiedDate`, `@Column(name = "updated_at", nullable = false)`.
- Enable auditing globally via [`JpaConfig.java`](../../backend/src/main/java/com/campushub/config/JpaConfig.java) with `@Configuration` and `@EnableJpaAuditing`.

### User Entity Contract (Agreed with Member 2)
- Create `com.campushub.user.entity.User` extending `BaseEntity`:
  - Table name: `@Table(name = "users")`
  - `id`: `Long`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - `email`: `String`, `@Column(nullable = false, unique = true, length = 100)`
  - `password`: `String`, `@Column(nullable = false, length = 255)` (stores BCrypt hash)
  - `role`: `String`, `@Column(nullable = false, length = 30)` (default `"ROLE_USER"`)
  - `enabled`: `boolean`, `@Column(nullable = false)` (default `true`)

### User Repository Contract
- Create `com.campushub.user.repository.UserRepository` extending `JpaRepository<User, Long>`:
  - `Optional<User> findByEmail(String email)`
  - `boolean existsByEmail(String email)`

### Security & Data Safety
- Enforce the unique `email` constraint at the database index level to prevent concurrent duplicate registration races.
- Never log raw passwords or sensitive database parameters.

### Backend Conventions
- Package layout must strictly follow:
  - `com.campushub.user.entity`
  - `com.campushub.user.repository`
  - `com.campushub.common.entity`
- All persistent entities across future business modules must inherit from `BaseEntity`.
- Database table and column names must strictly use `snake_case`.

### Testing
- Write `UserRepositoryTest` using `@DataJpaTest`:
  - Verify user can be saved and found by email.
  - Verify duplicate email insertion throws `DataIntegrityViolationException`.
  - Verify `createdAt` and `updatedAt` timestamps are automatically populated on insert.
- Ensure test suite passes via `./mvnw test`.

---

## Notes
- Do not implement Profile, Community, Marketplace, or Lost & Found business logic in this issue.
- Coordinate the `User` entity field names and types with Member 2 (Security) before merging.

---

## Deliverables
- Database configuration in `backend/src/main/resources/application.properties`.
- `BaseEntity.java` in `com.campushub.common.entity`.
- `JpaConfig.java` in `com.campushub.config`.
- `User.java` in `com.campushub.user.entity`.
- `UserRepository.java` in `com.campushub.user.repository`.
- `UserRepositoryTest.java` in `src/test/java/com/campushub/user/repository/`.
- PR targeting `developV2`.

---

## Definition of Done
- [ ] Application connects to MySQL using environment variables.
- [ ] `BaseEntity` automatically populates audit timestamps on insert and update.
- [ ] `User` entity and `UserRepository` are implemented and verified.
- [ ] Database-level unique constraint on `email` is verified.
- [ ] Automated tests pass (`./mvnw test`).
- [ ] PR created and reviewed against `developV2`.

---

## Suggested Branch
`feature/DB-001-database-persistence-foundation`

## Suggested Commit
`feat(database): configure mysql jpa and user persistence foundation`

