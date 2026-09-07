# Threat Model — CampusHub

## Document Metadata

| Field | Value |
|---|---|
| Document version | v1.0.0 — Initial Baseline Review |
| Review date | 2026-09-07 |
| Repository version | `daa968e` |
| Reviewer | Security Baseline Team |
| Status | Baseline Review — được review qua Pull Request |

## Version History

| Version | Date | Description |
|---|---|---|
| v1.0.0 | 2026-09-07 | Initial security baseline review |

---

## Scope

### In scope

- Backend API (Spring Boot)
- Frontend (React + TypeScript)
- Database (MySQL / H2)
- Authentication & Authorization
- File upload (tương lai)
- Community
- Marketplace
- Lost & Found

### Out of scope

- Third-party infrastructure
- Cloud provider infrastructure
- User's personal device
- Network / OS hardening của hosting provider

---

## Security Objectives

| Objective | Mô tả | Liên quan tới |
|---|---|---|
| **Confidentiality** | Bảo vệ dữ liệu người dùng, không để lộ thông tin cho người không có quyền | IDOR/BOLA, information exposure, token theft |
| **Integrity** | Ngăn sửa đổi dữ liệu trái phép | Unauthorized modification, price manipulation |
| **Availability** | Chống abuse/DoS, hệ thống luôn hoạt động | Rate limiting, spam/abuse, brute force |
| **Authentication** | Xác minh danh tính người dùng | Auth bypass, weak password storage |
| **Authorization** | Đảm bảo người dùng chỉ truy cập đúng quyền của mình | Privilege escalation, role tampering |
| **Accountability** | Có khả năng audit mọi hành động | Logging không lộ sensitive data |

---

## Risk Rating Methodology

Risk được đánh giá dựa trên công thức:

```
Likelihood × Impact = Risk
```

| Likelihood | Impact | Risk |
|---|---|---|
| High | Critical | **CRITICAL** |
| High | High | **HIGH** |
| Medium | High | **HIGH** |
| Low | High | **MEDIUM** |
| Low | Low | **LOW** |
| — | — | **INFO** |

---

## Assumptions

- Zero Trust: frontend không được tin, mọi request từ client đều có thể là malicious
- Attacker có thể gửi bất kỳ HTTP request nào
- Database chỉ được truy cập từ server-side

---

## 1. Authentication

### Attack Surface

| Threat | Risk | Description |
|---|---|---|
| Brute force | HIGH | Thử nhiều lần password cho đến khi đúng |
| Credential stuffing | HIGH | Dùng list password từ database leak khác |
| Account enumeration | MEDIUM | Biết được email nào tồn tại qua thông báo lỗi |
| Weak password storage | HIGH | Lưu plaintext hoặc hash yếu (MD5/SHA1) |
| Token theft | HIGH | Đánh cắp JWT/session token |
| Token reuse | MEDIUM | Dùng lại token đã hết hạn hoặc bị thu hồi |
| Session fixation | MEDIUM | Bắt buộc user dùng session do attacker tạo |
| Privilege escalation | CRITICAL | User thường tự gán quyền ADMIN |
| Role tampering | CRITICAL | User gửi role=ADMIN trong request |
| Auth bypass | CRITICAL | Truy cập endpoint bảo vệ mà không cần xác thực |

### Files affected

- `AuthController.java` — endpoint login, register, me, logout
- `AuthService.java` — business logic xác thực
- `SecurityConfig.java` — cấu hình bảo mật
- `PasswordConfig.java` — hashing algorithm

---

## 2. Community

### Attack Surface

| Threat | Risk | Description |
|---|---|---|
| Stored XSS | HIGH | Inject script vào post/comment hiển thị cho người khác |
| IDOR/BOLA | HIGH | User A sửa/xóa post của user B |
| Unauthorized modification | HIGH | Sửa post/comment không phải của mình |
| Spam/abuse | MEDIUM | Đăng bài spam, quảng cáo |
| Malicious input | MEDIUM | Gửi input độc hại qua form |
| Comment injection | MEDIUM | Attach link/malware trong comment |

### Files affected

- `CommunityController.java` (tương lai)
- `PostService.java` (tương lai)
- Frontend post/comment components (tương lai)

---

## 3. Marketplace

### Attack Surface

| Threat | Risk | Description |
|---|---|---|
| IDOR/BOLA | HIGH | User A sửa/xóa listing của user B |
| Parameter tampering | HIGH | Sửa giá, số lượng, trạng thái trong request |
| Business logic abuse | MEDIUM | Mua giá 0đ, mua âm, mua sản phẩm của mình |
| Unauthorized CRUD | HIGH | Tạo/sửa/xóa listing không có quyền |
| Malicious content | MEDIUM | Đăng listing chứa link/malware |
| Price manipulation | HIGH | Sửa giá sau khi xác nhận đơn |

### Files affected

- `MarketplaceController.java` (tương lai)
- `ListingService.java` (tương lai)
- Frontend marketplace components (tương lai)

---

## 4. Lost & Found

### Attack Surface

| Threat | Risk | Description |
|---|---|---|
| IDOR/BOLA | HIGH | User A sửa report của user B |
| Status tampering | HIGH | Đánh dấu "đã tìm thấy" cho report của người khác |
| Information exposure | MEDIUM | Lộ thông tin cá nhân qua report |
| Spam/fake report | MEDIUM | Đăng báo giả, lừa đảo |
| Malicious upload | HIGH | Upload file độc hại trong report |
| Unauthorized modification | HIGH | Sửa/xóa report không phải của mình |

### Files affected

- `LostFoundController.java` (tương lai)
- `ReportService.java` (tương lai)
- Frontend Lost & Found components (tương lai)

---

## 5. File Upload (Tương lai)

### Attack Surface

| Threat | Risk | Description |
|---|---|---|
| Path traversal | CRITICAL | `../../etc/passwd` — truy cập file hệ thống |
| Webshell upload | CRITICAL | Upload .php/.jsp để chạy code trên server |
| SVG script injection | HIGH | SVG chứa JavaScript chạy trong trình duyệt |
| Oversized file | MEDIUM | Upload file quá lớn gây crash server |
| Dangerous extension | HIGH | Upload .exe, .sh, .bat |
| Filename manipulation | MEDIUM | Tên file chứa `../` hoặc ký tự đặc biệt |
| MIME type bypass | MEDIUM | Đổi extension giả dạng file khác |

---

## 6. Database

### Attack Surface

| Threat | Risk | Description |
|---|---|---|
| SQL injection | CRITICAL | Nối chuỗi query từ user input |
| ORM bypass | HIGH | Dùng native query không parameterized |
| Error leak | MEDIUM | Database error message lộ schema/internal info |
| Data exposure | HIGH | Query trả về nhiều field hơn cần thiết |

---

## 7. Secrets & Config

### Attack Surface

| Threat | Risk | Description |
|---|---|---|
| API key commit | CRITICAL | API key bị commit vào repo |
| DB password commit | CRITICAL | Database password bị commit vào repo |
| JWT secret leak | CRITICAL | Secret token bị lộ → sign JWT giả |
| .env commit | CRITICAL | File `.env` chứa credentials bị commit |
| Private key leak | CRITICAL | Private key bị lộ |
| Log sensitive data | HIGH | Logging password, token, secret |
| Access token leak | HIGH | Access token bị lộ trong code/log |

---

## 8. Summary

| Module | Critical Risk | High Risk | Medium Risk |
|---|---|---|---|
| Authentication | 3 | 4 | 3 |
| Community | 0 | 3 | 3 |
| Marketplace | 0 | 4 | 3 |
| Lost & Found | 0 | 3 | 3 |
| File Upload | 2 | 2 | 3 |
| Database | 1 | 2 | 1 |
| Secrets | 4 | 1 | 0 |
| **Total** | **10** | **19** | **16** |

---

## 9. Baseline Review Summary (Kế quả rà soát thực tế Repo)

- **Review date:** 2026-09-07
- **Repository version:** `daa968e`
- **Reviewer:** Security Baseline Team
- **Status:** Initial Security Review

Kết quả rà soát bảo mật cho codebase hiện tại của CampusHub:

### 🟢 Điểm Tốt (Passed)
- **Không lộ Secrets**: Không phát hiện API keys, JWT secrets, database passwords bị commit. Mọi credentials đều dùng biến môi trường qua `.env.example`.
- **An toàn SQL Injection**: Sử dụng Spring Data JPA / Hibernate với Parameterized Queries toàn bộ.
- **XSS Prevention**: React auto-escaping trên Frontend, không sử dụng `dangerouslySetInnerHTML`.
- **Authentication**: Dùng BCrypt hash password, session fixation protection, CSRF token rotation, phân quyền server-side (USER/ADMIN).

### 🔴 Lỗ Hổng Cần Khắc Phục (Discovered Issues)

| Severity | Issue | Vị trí | Mô tả / Hướng khắc phục |
|---|---|---|---|
| **HIGH** | Thiếu Rate Limiting | Backend Auth | Endpoint `/api/v1/auth/login` và `register` chưa có rate limit, dễ bị Brute Force / Credential Stuffing. Cần bổ sung Bucket4j hoặc RateLimitFilter. |
| **MEDIUM** | SQL Query Logging bật ở Prod | `application.properties:8-9` | `spring.jpa.show-sql=true` có thể làm lộ thông tin database schema qua log. Nên đổi thành `false`. |
| **MEDIUM** | Unencrypted DB Connection | `application.properties:3` | `useSSL=false` trong MySQL connection string. Cần đổi thành `true` khi deploy production. |
| **MEDIUM** | Hardcoded CORS Origin | `SecurityConfig.java:208` | CORS hardcode `http://localhost:5173`. Cần đưa origin ra biến môi trường. |
| **MEDIUM** | Subdirectory `.gitignore` thiếu `.env` | `backend/.gitignore`, `frontend/.gitignore` | Thư mục con chưa có rule `.env` (hiện phụ thuộc `.gitignore` ở root). Nên bổ sung trực tiếp. |
| **LOW** | Hardcoded Credentials trong CI | `.github/workflows/ci.yml` | Dùng `root/root` cho MySQL container trong CI (chấp nhận được cho ephemeral CI nhưng cần lưu ý). |
| **INFO** | Thiếu HTTP Security Headers | `SecurityConfig.java` | Chưa cấu hình Content-Security-Policy, X-Frame-Options, HSTS. |

