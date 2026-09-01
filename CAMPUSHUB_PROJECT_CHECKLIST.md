# CampusHub — Master Project Checklist & Progress Tracker

> **Mục đích:** Đây là file theo dõi tiến độ trung tâm của CampusHub. Mỗi member chỉ cần mở file này để biết dự án đang ở đâu, phần nào đã xong, phần nào đang làm, phần nào bị chặn và task tiếp theo là gì.
>
> **Source of truth:** branch `develop`
>
> **Last updated:** 2026-09-01

---

## 1. Quy ước trạng thái

| Ký hiệu | Trạng thái | Ý nghĩa |
|---|---|---|
| ✅ | DONE | Đã hoàn thành, đã review/test và merge vào `develop` |
| 🟡 | IN PROGRESS | Đang có người thực hiện |
| 🟠 | PARTIAL | Đã làm một phần nhưng chưa thể coi là hoàn thành |
| 🔍 | REVIEW | Đã code xong, đang chờ review/PR |
| 🧪 | TESTING | Đang kiểm thử |
| ⛔ | BLOCKED | Bị chặn bởi dependency/task khác |
| ⬜ | TODO | Chưa bắt đầu |
| ❌ | DROPPED | Không còn nằm trong scope hiện tại |

### Cách update file

Khi nhận task, member nên cập nhật theo mẫu:

```text
Status: 🟡 IN PROGRESS
Owner: @github-username
Branch: feature/...
PR: #...
Note: mô tả ngắn dependency hoặc tình trạng hiện tại
```

Khi PR được merge vào `develop`:

```text
Status: ✅ DONE
PR: #...
```

Không đánh dấu `✅ DONE` chỉ vì code chạy ở local. Một task chỉ DONE khi đã đạt **Definition of Done** ở cuối file.

---

# 2. Snapshot hiện tại

## Tổng quan

| Hạng mục | Trạng thái | Ghi chú |
|---|---|---|
| Repository / Git workflow | ✅ DONE | `main`, `develop`, feature branches đã được sử dụng |
| Frontend foundation | ✅ DONE | React + TypeScript + Vite |
| Backend foundation | ✅ DONE | Spring Boot + Maven + Java 21 |
| CI foundation | ✅ DONE | Frontend/backend checks đã có |
| Frontend navigation | ✅ DONE | Routing, layout, navbar, 404 |
| Frontend Authentication — foundation | ✅ DONE | Login/Register/AuthContext/ProtectedRoute/authService |
| Backend Authentication | ⬜ TODO | Task quan trọng tiếp theo |
| Authentication end-to-end | ⛔ BLOCKED | Chờ Backend Authentication |
| Community | ⬜ TODO | Chưa triển khai nghiệp vụ |
| Marketplace | ⬜ TODO | Chưa triển khai nghiệp vụ |
| Lost & Found | ⬜ TODO | Chưa triển khai nghiệp vụ |
| Profile / User management | 🟠 PARTIAL | Route/profile placeholder + auth protection đã có |
| File Upload | ⬜ TODO | Chưa triển khai |
| Search / Filtering | ⬜ TODO | Chưa triển khai |
| Notifications | ⬜ TODO | Chưa triển khai |
| Admin / Moderation | ⬜ TODO | Chưa triển khai |
| Security hardening | ⬜ TODO | Chưa đến phase audit đầy đủ |
| Automated testing | 🟠 PARTIAL | Có foundation/health checks, chưa có test suite feature đầy đủ |
| Production deployment | ⬜ TODO | Chưa triển khai |

## PR/milestone đã hoàn thành nổi bật

- ✅ Project setup — PR #1
- ✅ Backend health check — PR #2
- ✅ Frontend health check — PR #4
- ✅ CI frontend/backend checks — PR #5
- ✅ Frontend routing foundation — PR #8
- ✅ Frontend navigation — PR #9
- ✅ Frontend Authentication foundation — PR #11, đã merge vào `develop`

---

# 3. Milestone 0 — Project Foundation

## 3.1 Repository & Collaboration

- [x] ✅ Khởi tạo GitHub repository
- [x] ✅ Tạo `main`
- [x] ✅ Tạo `develop`
- [x] ✅ Áp dụng feature branch workflow
- [x] ✅ Có `.gitignore`
- [x] ✅ Có `README.md`
- [x] ✅ Có `CONTRIBUTING.md`
- [x] ✅ Có `CODE_OF_CONDUCT.md`
- [x] ✅ Có `SECURITY.md`
- [x] ✅ Có LICENSE
- [x] ✅ Có Issue templates
- [x] ✅ Có Pull Request template
- [ ] ⬜ Chuẩn hóa GitHub labels (`frontend`, `backend`, `security`, `bug`, `feature`, `priority:*`, ...)
- [ ] ⬜ Tạo GitHub milestones/releases
- [ ] ⬜ Tạo Project Board/Kanban nếu team cần
- [ ] ⬜ Document branch naming convention đầy đủ
- [ ] ⬜ Document commit convention đầy đủ

## 3.2 CI/CD Foundation

- [x] ✅ GitHub Actions foundation
- [x] ✅ Frontend lint check
- [x] ✅ Frontend build check
- [x] ✅ Backend build/test check
- [ ] ⬜ Thêm test coverage reporting
- [ ] ⬜ Thêm dependency/security scanning
- [ ] ⬜ Thêm secret scanning workflow nếu cần
- [ ] ⬜ Thêm staging deploy workflow
- [ ] ⬜ Thêm production deploy workflow

---

# 4. Milestone 1 — Architecture & Technical Foundation

## 4.1 Frontend Foundation

- [x] ✅ React application initialized
- [x] ✅ TypeScript configured
- [x] ✅ Vite configured
- [x] ✅ Oxlint configured
- [x] ✅ React Router installed/configured
- [x] ✅ Application routes separated into `AppRoutes`
- [x] ✅ Main layout/navigation foundation
- [x] ✅ 404 page
- [x] ✅ Environment variable example (`VITE_API_BASE_URL`)
- [ ] ⬜ Chuẩn hóa reusable UI components (`Button`, `Input`, `Modal`, `Card`, ...)
- [ ] ⬜ Chọn/thiết lập styling strategy chính thức
- [ ] ⬜ Responsive layout foundation
- [ ] ⬜ Global loading/error UI
- [ ] ⬜ Toast/notification UI foundation
- [ ] ⬜ API client abstraction chung nếu dự án cần

## 4.2 Backend Foundation

- [x] ✅ Spring Boot application initialized
- [x] ✅ Maven wrapper
- [x] ✅ Java 21
- [x] ✅ Spring Web MVC
- [x] ✅ Spring Data JPA
- [x] ✅ Spring Validation
- [x] ✅ MySQL connector
- [x] ✅ Database connection config thông qua env
- [x] ✅ Health endpoint
- [ ] ⬜ Chuẩn hóa package structure cho feature/domain
- [ ] ⬜ Global API response/error format
- [ ] ⬜ Global exception handler
- [ ] ⬜ Database migrations bằng Flyway/Liquibase
- [ ] ⬜ Tách config theo environment (`dev`, `test`, `prod`)
- [ ] ⬜ Logging convention
- [ ] ⬜ Audit timestamps (`createdAt`, `updatedAt`) foundation

---

# 5. Milestone 2 — Authentication & Authorization

> **Tình trạng:** Frontend foundation đã merge. Backend Auth là ưu tiên kế tiếp. Full Auth chưa DONE cho đến khi chạy end-to-end.

## 5.1 Frontend Authentication

- [x] ✅ Login page
- [x] ✅ Login form
- [x] ✅ Login client validation
- [x] ✅ Register page
- [x] ✅ Register form
- [x] ✅ Register client validation
- [x] ✅ Email validation
- [x] ✅ Password length validation
- [x] ✅ Confirm password validation
- [x] ✅ Loading state khi submit
- [x] ✅ Server error state
- [x] ✅ `authService.ts`
- [x] ✅ `login()` API function
- [x] ✅ `register()` API function
- [x] ✅ `getCurrentUser()` API function
- [x] ✅ `logout()` API function
- [x] ✅ `AuthContext`
- [x] ✅ `AuthProvider`
- [x] ✅ `useAuth()` hook
- [x] ✅ Restore session logic khi reload app
- [x] ✅ `ProtectedRoute`
- [x] ✅ `/profile` được bảo vệ
- [x] ✅ Navbar thay đổi theo trạng thái login
- [x] ✅ Logout UI/action foundation
- [ ] ⛔ Test login với backend thật
- [ ] ⛔ Test register với backend thật
- [ ] ⛔ Test restore session với backend thật
- [ ] ⛔ Test logout với backend thật
- [ ] ⛔ Test error mapping thật từ backend
- [ ] ⛔ Redirect về route ban đầu sau khi login
- [ ] ⛔ End-to-end Auth test

### Ghi chú hiện tại

Frontend đang chuẩn bị gọi:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me
POST /api/v1/auth/logout
```

Và đang sử dụng `credentials: 'include'`, tức kiến trúc hiện tại thiên về cookie/session.

## 5.2 Backend Authentication — NEXT PRIORITY

**Suggested branch:** `feature/backend-authentication`

- [ ] ⬜ Thêm Spring Security dependency
- [ ] ⬜ Xác định auth strategy chính thức: session cookie hiện tại
- [ ] ⬜ Tạo `User` entity
- [ ] ⬜ Tạo `Role` enum/model (`USER`, `ADMIN`)
- [ ] ⬜ Tạo `UserRepository`
- [ ] ⬜ Unique constraint cho email
- [ ] ⬜ Normalize email
- [ ] ⬜ Password hashing bằng BCrypt/PasswordEncoder
- [ ] ⬜ DTO `RegisterRequest`
- [ ] ⬜ DTO `LoginRequest`
- [ ] ⬜ DTO/Auth response không trả password
- [ ] ⬜ Backend validation cho register
- [ ] ⬜ Backend validation cho login
- [ ] ⬜ `AuthService`
- [ ] ⬜ `AuthController`
- [ ] ⬜ `POST /api/v1/auth/register`
- [ ] ⬜ `POST /api/v1/auth/login`
- [ ] ⬜ `GET /api/v1/auth/me`
- [ ] ⬜ `POST /api/v1/auth/logout`
- [ ] ⬜ Session configuration
- [ ] ⬜ Cookie security configuration (`HttpOnly`, `SameSite`, `Secure` cho prod)
- [ ] ⬜ CORS config cho frontend origin
- [ ] ⬜ `401 Unauthorized` response chuẩn
- [ ] ⬜ `409 Conflict` cho duplicate email
- [ ] ⬜ Rate limiting/brute force protection strategy
- [ ] ⬜ Authentication integration tests
- [ ] ⬜ Authorization tests

## 5.3 Authentication Integration

- [ ] ⛔ Register frontend → backend → database
- [ ] ⛔ Login frontend → backend → session
- [ ] ⛔ Reload browser vẫn nhận user từ `/auth/me`
- [ ] ⛔ Profile chỉ truy cập khi authenticated
- [ ] ⛔ Logout invalidates session
- [ ] ⛔ Sau logout `/profile` redirect `/login`
- [ ] ⛔ Duplicate email hiển thị đúng lỗi
- [ ] ⛔ Wrong password hiển thị message an toàn
- [ ] ⛔ Kiểm tra session fixation
- [ ] ⛔ Kiểm tra CSRF strategy

---

# 6. Milestone 3 — User Profile & Account

## Backend

- [ ] ⬜ User profile model hoàn chỉnh
- [ ] ⬜ Get current profile API
- [ ] ⬜ Update full name/profile fields
- [ ] ⬜ Avatar metadata
- [ ] ⬜ Change password
- [ ] ⬜ Account status (`ACTIVE`, `SUSPENDED`, ...)
- [ ] ⬜ Authorization: user chỉ sửa profile của chính mình

## Frontend

- [ ] ⬜ Profile UI thực tế
- [ ] ⬜ Edit profile form
- [ ] ⬜ Avatar upload UI
- [ ] ⬜ Change password UI
- [ ] ⬜ Account settings
- [ ] ⬜ Error/loading/success feedback

---

# 7. Milestone 4 — Community

## 7.1 Backend Community

- [ ] ⬜ Post entity/model
- [ ] ⬜ Post repository
- [ ] ⬜ Post service
- [ ] ⬜ Create post API
- [ ] ⬜ Get feed API
- [ ] ⬜ Get post detail API
- [ ] ⬜ Update own post API
- [ ] ⬜ Delete own post API
- [ ] ⬜ Pagination
- [ ] ⬜ Comment model
- [ ] ⬜ Create comment
- [ ] ⬜ Delete own comment
- [ ] ⬜ Like/reaction model
- [ ] ⬜ Like/unlike API
- [ ] ⬜ Authorization rules
- [ ] ⬜ Input validation
- [ ] ⬜ Community backend tests

## 7.2 Frontend Community

- [ ] ⬜ Community feed page
- [ ] ⬜ Post card component
- [ ] ⬜ Create post form
- [ ] ⬜ Edit post
- [ ] ⬜ Delete post confirmation
- [ ] ⬜ Post detail page/modal
- [ ] ⬜ Comment list
- [ ] ⬜ Add comment
- [ ] ⬜ Like/unlike UI
- [ ] ⬜ Pagination/infinite scroll
- [ ] ⬜ Loading skeleton
- [ ] ⬜ Empty state
- [ ] ⬜ Error state
- [ ] ⬜ Responsive UI

---

# 8. Milestone 5 — Marketplace

## 8.1 Backend Marketplace

- [ ] ⬜ Marketplace item entity
- [ ] ⬜ Category model
- [ ] ⬜ Item status (`AVAILABLE`, `SOLD`, `HIDDEN`)
- [ ] ⬜ Create listing API
- [ ] ⬜ Update own listing
- [ ] ⬜ Delete own listing
- [ ] ⬜ Mark as sold
- [ ] ⬜ Listing detail API
- [ ] ⬜ Listing feed API
- [ ] ⬜ Pagination
- [ ] ⬜ Search/filter by keyword
- [ ] ⬜ Filter by category
- [ ] ⬜ Filter/sort by price/date
- [ ] ⬜ Ownership authorization
- [ ] ⬜ Marketplace tests

## 8.2 Frontend Marketplace

- [ ] ⬜ Marketplace listing page
- [ ] ⬜ Item card
- [ ] ⬜ Item detail page
- [ ] ⬜ Create listing form
- [ ] ⬜ Edit listing form
- [ ] ⬜ Delete listing
- [ ] ⬜ Mark sold
- [ ] ⬜ Search bar
- [ ] ⬜ Category filter
- [ ] ⬜ Price/date sorting
- [ ] ⬜ Pagination/infinite scroll
- [ ] ⬜ Loading/empty/error states
- [ ] ⬜ Responsive UI

---

# 9. Milestone 6 — Lost & Found

## 9.1 Backend Lost & Found

- [ ] ⬜ Lost/Found item model
- [ ] ⬜ Type: `LOST` / `FOUND`
- [ ] ⬜ Status lifecycle
- [ ] ⬜ Create report API
- [ ] ⬜ Update own report
- [ ] ⬜ Delete own report
- [ ] ⬜ Get report detail
- [ ] ⬜ Feed/list API
- [ ] ⬜ Search/filter
- [ ] ⬜ Filter Lost/Found
- [ ] ⬜ Mark resolved/returned
- [ ] ⬜ Ownership authorization
- [ ] ⬜ Lost & Found tests

## 9.2 Frontend Lost & Found

- [ ] ⬜ Lost & Found feed
- [ ] ⬜ Report card
- [ ] ⬜ Report detail
- [ ] ⬜ Create Lost report
- [ ] ⬜ Create Found report
- [ ] ⬜ Edit report
- [ ] ⬜ Delete report
- [ ] ⬜ Mark resolved
- [ ] ⬜ Search/filter UI
- [ ] ⬜ Loading/empty/error states
- [ ] ⬜ Responsive UI

---

# 10. Milestone 7 — File & Image Upload

> Dùng chung cho avatar, Community, Marketplace và Lost & Found.

## Backend

- [ ] ⬜ Xác định storage strategy (local dev / object storage production)
- [ ] ⬜ Upload endpoint/service abstraction
- [ ] ⬜ File size limit
- [ ] ⬜ Allowed MIME types
- [ ] ⬜ Extension validation
- [ ] ⬜ Magic byte/signature validation
- [ ] ⬜ Randomized safe filenames
- [ ] ⬜ Không lưu executable trong web root
- [ ] ⬜ Remove orphaned files
- [ ] ⬜ Delete files when entity deleted nếu policy yêu cầu
- [ ] ⬜ Access authorization nếu file private
- [ ] ⬜ Upload security tests

## Frontend

- [ ] ⬜ File picker component
- [ ] ⬜ Image preview
- [ ] ⬜ Client size/type hints
- [ ] ⬜ Upload progress/loading
- [ ] ⬜ Upload error feedback
- [ ] ⬜ Remove/replace selected image

---

# 11. Milestone 8 — Search, Discovery & Filters

- [ ] ⬜ Global search requirements
- [ ] ⬜ Community search
- [ ] ⬜ Marketplace search
- [ ] ⬜ Lost & Found search
- [ ] ⬜ Pagination strategy
- [ ] ⬜ Sorting strategy
- [ ] ⬜ Database indexes cho trường search/filter
- [ ] ⬜ Empty results UI
- [ ] ⬜ Query parameter synchronization trên frontend
- [ ] ⬜ Search performance tests

---

# 12. Milestone 9 — Notifications

> Có thể để sau MVP nếu scope cần giảm.

- [ ] ⬜ Define notification events
- [ ] ⬜ Notification entity
- [ ] ⬜ Read/unread state
- [ ] ⬜ Notification list API
- [ ] ⬜ Mark read API
- [ ] ⬜ Frontend notification center
- [ ] ⬜ Notification badge
- [ ] ⬜ Optional real-time strategy (SSE/WebSocket)
- [ ] ⬜ Notification tests

---

# 13. Milestone 10 — Admin & Moderation

## Authorization

- [ ] ⬜ Role `USER`
- [ ] ⬜ Role `ADMIN`
- [ ] ⬜ Backend method/endpoint authorization
- [ ] ⬜ Admin-only frontend routes

## Moderation

- [ ] ⬜ Report content model
- [ ] ⬜ Report post
- [ ] ⬜ Report marketplace listing
- [ ] ⬜ Report lost/found listing
- [ ] ⬜ Admin reports queue
- [ ] ⬜ Hide/remove content
- [ ] ⬜ Suspend user
- [ ] ⬜ Audit moderation actions
- [ ] ⬜ Admin dashboard
- [ ] ⬜ Moderation tests

---

# 14. Milestone 11 — Security

> Security không phải task cuối cùng duy nhất. Một số kiểm tra nên diễn ra xuyên suốt; phase này là hardening/audit đầy đủ trước release.

## 14.1 Threat Modeling

- [ ] ⬜ Vẽ/xác định attack surface Authentication
- [ ] ⬜ Attack surface Community
- [ ] ⬜ Attack surface Marketplace
- [ ] ⬜ Attack surface Lost & Found
- [ ] ⬜ Threat model File Upload
- [ ] ⬜ Threat model Admin endpoints
- [ ] ⬜ Liệt kê assets, trust boundaries, attacker goals

## 14.2 Authentication & Authorization Review

- [ ] ⬜ Review Spring Security config
- [ ] ⬜ Review session/JWT design
- [ ] ⬜ Review login/register flow
- [ ] ⬜ Review password hashing
- [ ] ⬜ Review session invalidation
- [ ] ⬜ Review USER/ADMIN authorization
- [ ] ⬜ Test auth bypass
- [ ] ⬜ Test privilege escalation
- [ ] ⬜ Test session fixation
- [ ] ⬜ Test CSRF

## 14.3 API Security Testing

- [ ] ⬜ IDOR/BOLA tests
- [ ] ⬜ Parameter tampering
- [ ] ⬜ SQL Injection tests
- [ ] ⬜ XSS tests
- [ ] ⬜ Mass assignment/over-posting
- [ ] ⬜ Input validation bypass
- [ ] ⬜ Rate limiting tests
- [ ] ⬜ Authentication brute force tests
- [ ] ⬜ HTTP method authorization
- [ ] ⬜ Sensitive data exposure review
- [ ] ⬜ Burp Suite/Postman security test collection

## 14.4 Backend Security Audit

- [ ] ⬜ Controller review
- [ ] ⬜ Service review
- [ ] ⬜ Repository/query review
- [ ] ⬜ Business logic flaw review
- [ ] ⬜ Insecure endpoint review
- [ ] ⬜ Error message leakage review
- [ ] ⬜ Logging sensitive data review

## 14.5 File Upload Security

- [ ] ⬜ MIME validation
- [ ] ⬜ Magic bytes validation
- [ ] ⬜ Extension allowlist
- [ ] ⬜ Maximum size
- [ ] ⬜ Dangerous filename handling
- [ ] ⬜ Storage isolation
- [ ] ⬜ SVG/script risk decision
- [ ] ⬜ Polyglot file consideration

## 14.6 DevSecOps

- [ ] ⬜ Dependency scanning frontend
- [ ] ⬜ Dependency scanning backend
- [ ] ⬜ Secret scanning
- [ ] ⬜ SAST/code scanning
- [ ] ⬜ CI security gates
- [ ] ⬜ Container scanning nếu dùng Docker
- [ ] ⬜ Vulnerability report template
- [ ] ⬜ Security test cases documented

---

# 15. Milestone 12 — Testing & Quality Assurance

## 15.1 Backend Tests

- [ ] ⬜ Auth unit tests
- [ ] ⬜ Auth integration tests
- [ ] ⬜ User/profile tests
- [ ] ⬜ Community service tests
- [ ] ⬜ Community API tests
- [ ] ⬜ Marketplace tests
- [ ] ⬜ Lost & Found tests
- [ ] ⬜ File upload tests
- [ ] ⬜ Admin authorization tests
- [ ] ⬜ Validation/error tests

## 15.2 Frontend Tests

- [ ] ⬜ Chọn frontend test stack (Vitest/RTL hoặc tương đương)
- [ ] ⬜ Login form tests
- [ ] ⬜ Register form tests
- [ ] ⬜ ProtectedRoute tests
- [ ] ⬜ Community UI tests
- [ ] ⬜ Marketplace UI tests
- [ ] ⬜ Lost & Found UI tests
- [ ] ⬜ Profile UI tests
- [ ] ⬜ Error/loading state tests

## 15.3 End-to-End Tests

- [ ] ⬜ Chọn E2E tool
- [ ] ⬜ Register → Login → Logout
- [ ] ⬜ Protected profile flow
- [ ] ⬜ Community create/edit/delete flow
- [ ] ⬜ Marketplace create/edit/sold flow
- [ ] ⬜ Lost & Found create/resolve flow
- [ ] ⬜ Admin moderation critical flow

## 15.4 Manual QA

- [ ] ⬜ Desktop Chrome
- [ ] ⬜ Desktop Firefox
- [ ] ⬜ Edge
- [ ] ⬜ Mobile responsive check
- [ ] ⬜ Keyboard navigation basic check
- [ ] ⬜ Form accessibility check
- [ ] ⬜ Error recovery check

---

# 16. Milestone 13 — Performance & Reliability

- [ ] ⬜ Database indexes review
- [ ] ⬜ N+1 query review
- [ ] ⬜ Pagination bắt buộc cho list lớn
- [ ] ⬜ Frontend bundle review
- [ ] ⬜ Image optimization strategy
- [ ] ⬜ API response time baseline
- [ ] ⬜ Load test critical endpoints
- [ ] ⬜ Database backup strategy
- [ ] ⬜ Error monitoring strategy
- [ ] ⬜ Health/readiness checks cho deploy

---

# 17. Milestone 14 — Documentation

- [ ] ⬜ README cập nhật kiến trúc hiện tại
- [ ] ⬜ Local setup guide frontend
- [ ] ⬜ Local setup guide backend
- [ ] ⬜ MySQL setup guide
- [ ] ⬜ `.env.example` đầy đủ
- [ ] ⬜ API documentation (OpenAPI/Swagger)
- [ ] ⬜ Authentication/session documentation
- [ ] ⬜ Database schema documentation
- [ ] ⬜ Architecture overview
- [ ] ⬜ Security assumptions/threat model
- [ ] ⬜ Contributor workflow
- [ ] ⬜ Deployment guide
- [ ] ⬜ Troubleshooting guide

---

# 18. Milestone 15 — Deployment & Release

## Backend

- [ ] ⬜ Production database
- [ ] ⬜ Production environment config
- [ ] ⬜ Secrets management
- [ ] ⬜ Production CORS config
- [ ] ⬜ Secure cookies/HTTPS
- [ ] ⬜ Database migration strategy
- [ ] ⬜ Backend deployment
- [ ] ⬜ Health check verified

## Frontend

- [ ] ⬜ Production API URL
- [ ] ⬜ Production build
- [ ] ⬜ Frontend hosting
- [ ] ⬜ SPA route fallback configured
- [ ] ⬜ HTTPS

## Release

- [ ] ⬜ Staging smoke test
- [ ] ⬜ Production smoke test
- [ ] ⬜ Security review passed
- [ ] ⬜ Critical E2E flows passed
- [ ] ⬜ Release notes
- [ ] ⬜ Version/tag created
- [ ] ⬜ `develop` → `main` release PR
- [ ] ⬜ Production release completed

---

# 19. Milestone 16 — Post-release

- [ ] ⬜ Monitor errors
- [ ] ⬜ Monitor uptime
- [ ] ⬜ Review security reports
- [ ] ⬜ Review user feedback
- [ ] ⬜ Bug triage process
- [ ] ⬜ Dependency update process
- [ ] ⬜ Backup restore test
- [ ] ⬜ Performance review
- [ ] ⬜ Plan next release

---

# 20. Current Priority Queue

> Đây là phần member nên đọc đầu tiên khi cần tìm việc tiếp theo.

| Priority | Task | Status | Suggested Branch | Dependency |
|---|---|---|---|---|
| P0 | Backend Authentication foundation | ⬜ TODO | `feature/backend-authentication` | Frontend Auth đã sẵn sàng |
| P0 | User entity + repository + password hashing | ⬜ TODO | `feature/backend-authentication` | Spring Security setup |
| P0 | Register/Login/Me/Logout API | ⬜ TODO | `feature/backend-authentication` | User/Auth service |
| P0 | Auth end-to-end integration | ⛔ BLOCKED | `feature/auth-integration` hoặc cùng auth branch | Backend Auth |
| P1 | Authentication security review cơ bản | ⛔ BLOCKED | `security/auth-review` | Backend Auth chạy được |
| P1 | User Profile implementation | ⬜ TODO | `feature/user-profile` | Auth end-to-end |
| P1 | Community MVP | ⬜ TODO | `feature/community` | Auth/User foundation |
| P1 | Marketplace MVP | ⬜ TODO | `feature/marketplace` | Auth/User + upload planning |
| P1 | Lost & Found MVP | ⬜ TODO | `feature/lost-found` | Auth/User + upload planning |
| P2 | File upload shared module | ⬜ TODO | `feature/file-upload` | Storage design |
| P2 | Admin/Moderation | ⬜ TODO | `feature/admin-moderation` | Main modules ổn định |
| P2 | Full security audit | ⛔ BLOCKED | `security/full-audit` | APIs/features đã đủ lớn |

---

# 21. Member Work Board

> Điền tên member/branch khi bắt đầu task. Không để hai người vô tình làm cùng một task mà không biết nhau.

| Task | Owner | Status | Branch | PR | Notes |
|---|---|---|---|---|---|
| Backend Authentication | — | ⬜ TODO | `feature/backend-authentication` | — | Task tiếp theo quan trọng nhất |
| Auth Security Review | — | ⛔ BLOCKED | — | — | Bắt đầu khi backend auth có endpoint |
| Community | — | ⬜ TODO | `feature/community` | — | Có thể tách FE/BE nếu team đông |
| Marketplace | — | ⬜ TODO | `feature/marketplace` | — | Cần quyết định image upload |
| Lost & Found | — | ⬜ TODO | `feature/lost-found` | — | Cần quyết định image upload |
| File Upload Security | — | ⬜ TODO | `feature/file-upload` | — | Có thể chuẩn bị threat model trước |

---

# 22. Dependency Map

```text
PROJECT FOUNDATION ✅
        │
        ├───────────────┐
        │               │
        ▼               ▼
FRONTEND FOUNDATION ✅   BACKEND FOUNDATION ✅
        │               │
        ▼               ▼
FRONTEND AUTH ✅      BACKEND AUTH ⬜  ← NEXT
        │               │
        └───────┬───────┘
                ▼
        AUTH INTEGRATION ⛔
                │
                ▼
          USER / PROFILE
                │
       ┌────────┼───────────┐
       ▼        ▼           ▼
   COMMUNITY  MARKETPLACE  LOST & FOUND
       │        │           │
       └────────┼───────────┘
                ▼
        SEARCH / UPLOAD / UX
                │
                ▼
        ADMIN & MODERATION
                │
                ▼
       SECURITY + FULL QA
                │
                ▼
          DEPLOY / RELEASE
```

---

# 23. Definition of Done (DoD)

Một task **chỉ được đánh dấu ✅ DONE** khi thỏa các điều kiện phù hợp dưới đây:

## Code

- [ ] Code đáp ứng acceptance criteria
- [ ] Không để debug code không cần thiết
- [ ] Không commit secret/password/token
- [ ] Naming và folder structure hợp lý
- [ ] Không phá vỡ feature đã có

## Frontend

- [ ] `npm run lint` pass
- [ ] `npm run build` pass
- [ ] UI được test thủ công
- [ ] Loading/error/empty state phù hợp
- [ ] Client validation phù hợp

## Backend

- [ ] Backend build pass
- [ ] Tests liên quan pass
- [ ] Validation ở server đầy đủ
- [ ] Authorization kiểm tra ở server, không dựa vào frontend
- [ ] API error không leak dữ liệu nhạy cảm

## Git / Collaboration

- [ ] Branch tạo từ `develop` mới nhất
- [ ] Commit message rõ ràng
- [ ] Push branch
- [ ] Pull Request target là `develop`
- [ ] Review xong
- [ ] CI pass
- [ ] PR merge vào `develop`
- [ ] File checklist này được cập nhật nếu task thay đổi trạng thái/milestone

---

# 24. Release Completion Checklist

CampusHub có thể coi là đạt **MVP hoàn chỉnh** khi tối thiểu các checkbox sau đều xong:

- [ ] Authentication end-to-end hoạt động
- [ ] Profile cơ bản hoạt động
- [ ] Community CRUD hoạt động
- [ ] Marketplace CRUD hoạt động
- [ ] Lost & Found CRUD hoạt động
- [ ] Upload ảnh an toàn cho feature cần ảnh
- [ ] Authorization ownership hoạt động đúng
- [ ] USER/ADMIN permission đúng scope
- [ ] Critical security findings đã xử lý
- [ ] Backend critical tests pass
- [ ] Frontend critical tests pass
- [ ] Critical E2E flows pass
- [ ] CI pass
- [ ] Documentation setup đầy đủ
- [ ] Staging smoke test pass
- [ ] Production deployment pass
- [ ] Release PR `develop → main` hoàn tất

---

# 25. Changelog tiến độ

> Thêm một dòng ngắn mỗi khi có milestone/PR quan trọng được merge.

| Date | Change | PR/Branch |
|---|---|---|
| 2026-08-24 | Initialize frontend + backend | PR #1 |
| 2026-08-25 | Backend health check | PR #2 |
| 2026-08-25 | Frontend health/navigation foundations | PR #4, #8, #9 |
| 2026-08-25 | CI frontend/backend checks | PR #5 |
| 2026-08-30 | Frontend Authentication foundation merged to `develop` | PR #11 |
| 2026-09-01 | Master project checklist created | `CAMPUSHUB_PROJECT_CHECKLIST.md` |

---

## Ghi chú cuối

File này nên nằm ở **root repository**:

```text
CampusHub/
├── .github/
├── backend/
├── frontend/
├── CAMPUSHUB_PROJECT_CHECKLIST.md  ← file này
├── README.md
└── ...
```

Mỗi PR lớn nên kiểm tra xem có cần update file này không. Mục tiêu là để bất kỳ member mới hay cũ nào chỉ cần đọc 3 phần:

1. **Snapshot hiện tại**
2. **Current Priority Queue**
3. **Member Work Board**

là biết ngay dự án đang ở đâu và nên làm gì tiếp theo.
