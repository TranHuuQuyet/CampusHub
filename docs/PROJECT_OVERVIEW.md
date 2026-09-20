# CampusHub — Project Overview

> **Purpose:** This document is the onboarding guide for anyone who is new to CampusHub. It explains what the project is, who it is for, how the main features work, how the system is structured, and how contributors should work with the repository.
>
> For detailed requirements and implementation progress, see `CAMPUSHUB_PROJECT_CHECKLIST.md`. For contribution rules, see `CONTRIBUTING.md`.

---

## 1. What is CampusHub?

CampusHub is a full-stack student-focused platform designed to connect students within and across universities.

The core idea is to provide one place where students can:

- communicate and share information;
- participate in university-specific or broader communities;
- buy and sell items with other students;
- report and find lost belongings;
- receive notifications about relevant activity;
- use the platform safely through authentication, authorization, moderation, and security controls.

CampusHub is being developed as a real software project rather than a one-off demo. The repository is organized so that additional contributors can understand the system, work on isolated features, review each other's changes, and gradually expand the platform.

---

## 2. The Problem CampusHub Solves

Student life creates many small communication and coordination problems:

- Useful information is spread across many unrelated groups and platforms.
- Students from the same university need a focused place to communicate.
- Students may also want to exchange information with people outside their own university.
- Students often need a simple way to buy or sell used items.
- Lost-and-found information can be difficult to discover or verify.
- Community content needs moderation and access control as the platform grows.

CampusHub brings these use cases into one platform with a shared technical foundation.

---

## 3. Who is CampusHub For?

### Primary users

Students who want to:

- join communities;
- publish and discuss posts;
- find or share useful campus information;
- buy or sell items;
- report lost or found belongings;
- manage their profile and account.

### Moderators / administrators

Trusted users who need tools to:

- review reports;
- moderate inappropriate content;
- manage users;
- perform administrative actions;
- maintain platform safety.

### Contributors

Developers who want to build the platform collaboratively through GitHub issues, feature branches, pull requests, reviews, testing, and documentation.

---

## 4. Product Scope

The current product direction is organized into these major areas:

| Module | Purpose |
|---|---|
| Authentication | Register, login, logout, current-user session, authorization |
| User / Profile | Manage identity and personal profile information |
| Community | Social discussion and information sharing |
| Marketplace | Buy and sell student-related items |
| Lost & Found | Report, search, and resolve lost/found items |
| Notifications | Inform users about relevant activity |
| Admin / Moderation | Reports, moderation, and administrative controls |
| File Upload | Secure handling of user-uploaded files/images |
| Search & Filtering | Find relevant posts, listings, and lost/found records |

The project may grow beyond this scope. New ideas should be discussed and added to the project plan before becoming implementation requirements.

---

# 5. How CampusHub Works

At a high level, the system follows this flow:

```
Student
   |
   | uses browser
   v
React Frontend
   |
   | HTTP / REST API
   v
Spring Boot Backend
   |
   +---- Authentication / Authorization
   |
   +---- Business Logic
   |
   +---- Validation
   |
   +---- Security
   |
   v
MySQL Database
```

The frontend is responsible mainly for the user interface and client-side behavior.

The backend is responsible for API endpoints, business rules, authentication, authorization, validation, persistence, and server-side security.

MySQL stores persistent application data.

---

# 6. Main User Flows

## 6.1 Authentication

A normal user flow is:

```
Register
   |
   v
Account created
   |
   v
Login
   |
   v
Authenticated session
   |
   +--> Access protected features
   |
   +--> Get current user
   |
   +--> Logout
```

The backend must never trust the frontend alone to enforce permissions. Authorization decisions must be enforced server-side.

Planned core authentication endpoints include:

```
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me
POST /api/v1/auth/logout
```

---

## 6.2 Community

Community is the social discussion area of CampusHub.

There are two important concepts:

1. **Focused communities** — for a particular university, group, or context.
2. **Shared/open communities** — broader spaces where students can exchange information across communities.

A typical flow is:

```
Student
   |
   +--> Browse communities
   |
   +--> Join / access community
   |
   +--> Create post
   |       |
   |       +--> Other users comment
   |       +--> Other users react
   |
   +--> View post detail
   |
   +--> Manage own content
```

The exact community rules and permissions are defined in the project checklist and implementation issues.

---

## 6.3 Marketplace

Marketplace allows students to publish items for sale and discover listings from other users.

Typical flow:

```
Seller
   |
   +--> Create listing
           |
           +--> Title
           +--> Description
           +--> Price
           +--> Category
           +--> Images
           +--> Status
```

A buyer can:

```
Buyer
   |
   +--> Search listings
   +--> Filter
   +--> Sort
   +--> View listing details
```

Listing ownership must be checked by the backend. A user must not be able to modify another user's listing simply by changing an ID in a request.

Listings can have lifecycle states such as available, sold, or hidden.

---

## 6.4 Lost & Found

Lost & Found helps students report belongings that have been lost or found.

Typical flow:

```
Student
   |
   +--> Report LOST item
   |
   +--> Report FOUND item
   |
   v
Search / filtering / discovery
   |
   v
Contact / verification flow
   |
   v
Resolve / returned
```

The system should distinguish between lost and found reports and maintain an appropriate status lifecycle.

---

## 6.5 Notifications

Notifications provide a central place for user activity that requires attention.

Examples may include:

- activity on a user's content;
- relevant community activity;
- moderation-related events;
- other application events that should be surfaced to the user.

The initial implementation can use normal API-based notification retrieval. Real-time delivery such as SSE or WebSocket can be considered as the project evolves.

---

## 6.6 Admin & Moderation

CampusHub is intended to be a community platform, so moderation is part of the system design.

Administrators/moderators may need to:

- review reported content;
- hide or remove inappropriate content;
- manage problematic accounts;
- suspend users where appropriate;
- review moderation history.

Administrative operations require explicit server-side authorization.

---

# 7. Authentication & Authorization Concepts

These concepts are different:

### Authentication

Answers:

> Who is this user?

Example:

```
email + password
       |
       v
    login
       |
       v
authenticated user
```

### Authorization

Answers:

> What is this user allowed to do?

Example:

```
USER
  └── can manage own post

ADMIN
  └── can perform administrative moderation actions
```

A frontend button being hidden is not security. The backend must verify authorization on every protected operation.

The project also considers:

- password hashing;
- input validation;
- session/cookie handling;
- CSRF protection where applicable;
- rate limiting;
- brute-force protection;
- privilege escalation prevention;
- IDOR/BOLA prevention.

---

# 8. Security Principles

Security is treated as a cross-cutting concern rather than a final step.

Important areas include:

- authentication bypass;
- authorization and privilege escalation;
- IDOR/BOLA;
- SQL injection;
- XSS;
- mass assignment;
- CSRF;
- brute-force attacks;
- rate limiting;
- file-upload attacks;
- unsafe file names;
- MIME-type spoofing;
- dependency vulnerabilities;
- accidental secret exposure.

For uploaded files, validation should consider more than the filename extension. The implementation should use appropriate MIME/type validation, file-size limits, safe naming, storage isolation, and authorization.

Secrets such as database passwords must never be committed to Git.

Local secrets belong in ignored environment files or environment variables. A safe example file can document the required variable names without containing real credentials.

---

# 9. Technical Architecture

## Frontend

Current direction:

- React
- TypeScript
- Vite

Responsibilities:

- render the user interface;
- handle user interaction;
- call backend APIs;
- manage client-side state;
- display loading/error/success states;
- perform appropriate client-side validation;
- provide responsive UI.

Client-side validation improves user experience but does not replace backend validation.

## Backend

Current direction:

- Java
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA
- MySQL Driver
- Validation

Responsibilities:

- expose REST APIs;
- authenticate users;
- authorize operations;
- validate input;
- implement business rules;
- access the database;
- handle errors consistently;
- enforce security controls.

## Database

Current database:

- MySQL

The backend uses JPA/Hibernate to map application entities to database tables.

Database design should be driven by the domain model and access patterns. Appropriate indexes should be added as search and filtering requirements become concrete.

---

# 10. API Architecture

The backend exposes versioned APIs under:

```
/api/v1/...
```

Examples:

```
GET /api/v1/health

POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me
POST /api/v1/auth/logout
```

The API layer should follow consistent conventions for:

- HTTP methods;
- status codes;
- request validation;
- response structures;
- error handling;
- authentication;
- authorization;
- pagination;
- filtering;
- sorting.

Do not expose database entities directly just because it is convenient. API request/response models should be designed intentionally as the application grows.

---

# 11. Project Structure

The repository currently follows this high-level structure:

```
CampusHub/
│
├── .github/
│   └── GitHub workflows, issue templates, and repository configuration
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── pom.xml
│   └── ...
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── ...
│
├── docs/
│   └── project documentation
│
├── CAMPUSHUB_PROJECT_CHECKLIST.md
├── CONTRIBUTING.md
├── SECURITY.md
├── CODE_OF_CONDUCT.md
├── LICENSE
└── README.md
```

As the codebase grows, feature-specific packages/modules should be organized consistently rather than placing unrelated code into large shared files.

---

# 12. Local Development

A contributor normally needs:

- Git;
- Node.js and npm;
- Java 21;
- Maven Wrapper provided by the backend;
- MySQL.

## Frontend

From the repository root:

```bash
cd frontend
npm install
npm run dev
```

The Vite development server normally runs on:

```
http://localhost:5173
```

## Backend

From the repository root:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows Command Prompt, the Maven wrapper can be invoked with:

```cmd
mvnw.cmd spring-boot:run
```

The backend normally runs on:

```
http://localhost:8080
```

## Database

Create the local CampusHub MySQL database and provide the required credentials through environment variables.

Never commit real credentials.

---

# 13. Environment Variables

The backend expects database configuration through environment variables rather than hard-coded secrets.

The important principle is:

```
application.properties
        |
        +--> DB_USERNAME
        +--> DB_PASSWORD
```

A contributor should create their own local environment configuration from the project's example file when one is provided.

Never copy another developer's real credentials into Git.

---

# 14. Development Workflow

CampusHub uses a branch-based Git workflow.

The important branches are:

```
main
  |
  └── develop
        |
        ├── feature/authentication
        ├── feature/community
        ├── feature/marketplace
        ├── feature/lost-found
        └── feature/<your-task>
```

### main

The stable/release branch.

### develop

The integration branch for completed feature work that is ready to be combined into the next release.

### feature branches

Short-lived branches created for a specific task or issue.

Examples:

```
feature/authentication
feature/community-posts
feature/marketplace-listing
feature/lost-found
docs/project-overview
fix/login-validation
```

Do not work directly on `main` for normal feature development.

---

# 15. Issue → Branch → PR Workflow

Every meaningful change should have a clear purpose.

The normal workflow is:

```
GitHub Issue
     |
     v
Create feature branch
     |
     v
Implement
     |
     v
Run tests / checks
     |
     v
Commit
     |
     v
Push branch
     |
     v
Pull Request → develop
     |
     v
Review
     |
     v
Merge
```

A good commit should describe one logical change.

Examples:

```
feat(auth): add login endpoint
fix(marketplace): validate listing ownership
docs: update contributor onboarding
test(auth): add registration service tests
chore(backend): update dependencies
```

---

# 16. Pull Request Expectations

A PR should explain:

1. What changed?
2. Why was it changed?
3. How was it tested?
4. Are there any known limitations?

Before opening a PR:

- make sure the code builds;
- run relevant tests;
- check formatting/linting;
- verify that secrets are not staged;
- keep the PR focused;
- avoid unrelated changes.

The target for normal feature work is:

```
feature branch → develop
```

Release work can later move:

```
develop → main
```

---

# 17. Testing Philosophy

Testing should exist at multiple levels.

### Backend

Potential layers include:

- unit tests;
- service tests;
- repository/integration tests;
- controller/API tests;
- security-related tests.

### Frontend

Potential layers include:

- component tests;
- integration tests;
- user-flow tests;
- end-to-end tests.

### Manual verification

For user-facing features, verify:

- normal flow;
- invalid input;
- unauthorized access;
- ownership boundaries;
- loading states;
- error states;
- responsive behavior.

A feature is not complete simply because the happy path works.

---

# 18. CI/CD and Quality Gates

The project is intended to use automated checks for changes entering shared branches.

Depending on the stage of the project, checks can include:

- frontend lint;
- frontend build;
- backend build;
- backend tests;
- dependency scanning;
- secret scanning;
- SAST/security checks;
- other repository quality gates.

Security checks should fail safely when required credentials or external services are unavailable rather than silently giving a false sense of security.

---

# 19. Current Development Direction

The project is intentionally being built in dependency order.

A simplified dependency flow is:

```
Project foundation
       |
       v
Backend authentication
       |
       v
User / Profile
       |
       +------------------+
       |                  |
       v                  v
Community            Marketplace
       |
       +------------------+
                          |
                          v
                    Lost & Found
                          |
                          v
                 Notifications /
                 Moderation /
                 Advanced features
```

Authentication and user identity are foundational because later modules need to know who created, owns, modifies, reports, or moderates data.

For the current detailed priority and implementation status, always check:

```
CAMPUSHUB_PROJECT_CHECKLIST.md
```

---

# 20. How a New Contributor Should Start

If you are new to CampusHub, do not try to understand the entire codebase in one sitting.

Follow this order:

### Step 1 — Understand the product

Read this document.

You should understand:

- what CampusHub is;
- who uses it;
- the major modules;
- how the frontend, backend, and database communicate.

### Step 2 — Read contribution rules

Read:

```
CONTRIBUTING.md
```

Understand the branch, commit, issue, and PR conventions.

### Step 3 — Set up the project

Run the frontend and backend locally.

Make sure the environment works before changing code.

### Step 4 — Read the current plan

Read:

```
CAMPUSHUB_PROJECT_CHECKLIST.md
```

This contains detailed requirements, dependencies, and progress tracking.

### Step 5 — Choose an issue

Pick an issue that is:

- unassigned or explicitly available;
- small enough to understand;
- compatible with the current project dependency order.

If you are unsure, ask before starting a large change.

### Step 6 — Create a branch

Create a branch from the latest `develop`.

Example:

```bash
git switch develop
git pull origin develop
git switch -c feature/<short-description>
```

### Step 7 — Implement and test

Make the smallest coherent change that solves the issue.

Run the relevant tests and checks.

### Step 8 — Open a PR

Push your branch and open a PR against `develop`.

Explain what changed and how it was tested.

---

# 21. Important Rules for Contributors

### Do

- Read the relevant issue before coding.
- Understand existing code before replacing it.
- Keep changes focused.
- Validate user input.
- Enforce authorization on the backend.
- Write tests for important behavior.
- Keep secrets out of Git.
- Update documentation when behavior changes.
- Ask questions when requirements are ambiguous.

### Do not

- Commit passwords, API keys, or private credentials.
- Work directly on `main` for normal feature development.
- Bypass backend authorization because the frontend already hides a button.
- Modify unrelated modules without a reason.
- Add large dependencies without justification.
- Treat client-side validation as a security boundary.
- Merge unfinished work into shared branches without discussion.

---

# 22. Design Principles

CampusHub should evolve around a few simple principles:

### 1. Security by design

Security requirements should be considered while designing a feature, not after implementation.

### 2. Clear separation of responsibilities

Frontend, backend, and database responsibilities should remain understandable.

### 3. Small, reviewable changes

Prefer focused issues and PRs over large unreviewable changes.

### 4. Explicit authorization

Every protected resource must have a clear ownership/permission model.

### 5. Maintainability

Code should be understandable by contributors who did not write the original feature.

### 6. Documentation

Important architectural and behavioral decisions should be documented.

### 7. Extensibility

The initial implementation should leave room for future improvements without unnecessarily over-engineering the first version.

---

# 23. Source of Truth

Different documents answer different questions:

| Document | Purpose |
|---|---|
| `README.md` | Quick repository introduction and entry point |
| `docs/PROJECT_OVERVIEW.md` | Understand the product, architecture, workflows, and onboarding |
| `CAMPUSHUB_PROJECT_CHECKLIST.md` | Detailed requirements, dependencies, priorities, and progress |
| `CONTRIBUTING.md` | Contribution rules and GitHub workflow |
| `SECURITY.md` | Security policy |
| `CODE_OF_CONDUCT.md` | Community behavior expectations |

When documents disagree:

1. Check the latest approved project requirement.
2. Check the relevant GitHub issue/PR discussion.
3. Clarify ambiguity before implementing a conflicting interpretation.

---

# 24. Where CampusHub Can Grow

The architecture is intentionally open to future features.

Possible areas of expansion include:

- richer community discovery;
- improved marketplace interaction;
- stronger lost-and-found matching;
- real-time notifications;
- messaging;
- university-specific features;
- richer moderation tools;
- analytics and administration;
- mobile clients;
- additional integrations.

These are potential directions, not automatic requirements. New scope should be discussed and tracked before implementation.

---

# 25. Final Mental Model

If you remember only one diagram, remember this:

```
                         CAMPUSHUB
                             |
              +--------------+--------------+
              |              |              |
          Community      Marketplace    Lost & Found
              |              |              |
              +--------------+--------------+
                             |
                         User Account
                             |
                     Authentication
                             |
                     Spring Boot API
                             |
                           MySQL
                             |
                       React Frontend
```

And from a developer's perspective:

```
Requirement
    ↓
GitHub Issue
    ↓
Feature Branch
    ↓
Implementation
    ↓
Tests
    ↓
Pull Request
    ↓
Review
    ↓
develop
    ↓
Release
    ↓
main
```

**CampusHub is a collaborative project. The goal is not only to make the software work, but to make the codebase understandable, secure, testable, and easy for the next contributor to continue.**
