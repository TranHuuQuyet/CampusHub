# Security Checklist — CampusHub

> Dùng checklist này khi phát triển bất kỳ module nào.
> Đánh dấu `[x]` khi đã hoàn thành, `[ ]` khi chưa.
> Không merge PR nếu các mục CRITICAL chưa được check.

## Document Metadata

| Field | Value |
|---|---|
| Document version | v1.0.0 — Initial Baseline Review |
| Review date | 2026-09-07 |
| Repository version | `daa968e` |
| Reviewer |  |
| Status | Baseline Review — được review qua Pull Request |

## Version History

| Version | Date | Description |
|---|---|---|
| v1.0.0 | 2026-09-07 | Initial security baseline review |

---

---

## 1. Authentication

- [ ] Không lưu password plaintext
- [ ] Dùng BCrypt/Argon2 để hash password
- [ ] Validate input phía server (email format, password length)
- [ ] Lỗi auth không lộ thông tin nhạy cảm
- [ ] Authorization enforced server-side
- [ ] User không thể tự gán quyền ADMIN
- [ ] Protected endpoints reject unauthenticated requests
- [ ] Session/JWT có thời hạn hợp lý
- [ ] Session invalidated khi logout
- [ ] Rate limiting cho login/register (chống brute force)

---

## 2. API Security

- [ ] Validate mọi request input
- [ ] Verify resource ownership (user chỉ sửa/xóa của mình)
- [ ] Chặn IDOR/BOLA
- [ ] Chặn unauthorized create/update/delete
- [ ] Không tin ID/role do frontend gửi
- [ ] Rate limiting cho sensitive endpoints
- [ ] HTTP method đúng (GET/POST/PUT/DELETE)
- [ ] Input length limit hợp lý
- [ ] Content-Type validation

---

## 3. Database

- [ ] Dùng ORM/repository có parameterized query
- [ ] Không nối chuỗi SQL từ user input
- [ ] Không leak database error ra client
- [ ] Không query nhiều field hơn cần thiết
- [ ] Unique constraint cho email
- [ ] Index cho trường search/filter

---

## 4. File Upload

- [ ] Whitelist MIME type (chỉ cho phép type an toàn)
- [ ] Magic-byte validation (kiểm tra header file thực sự)
- [ ] Extension validation (deny .php, .exe, .sh, .bat)
- [ ] Giới hạn file size
- [ ] Tên file ngẫu nhiên (không dùng tên gốc)
- [ ] Lưu ngoài web root
- [ ] Chặn path traversal (`../`, `..\\`)
- [ ] Không cho phép SVG chứa script
- [ ] Xóa file khi entity bị xóa

---

## 5. Secrets & Config

- [ ] Không commit API key vào repo
- [ ] Không commit DB password vào repo
- [ ] Không commit JWT secret vào repo
- [ ] Không commit private key vào repo
- [ ] Không commit access token vào repo
- [ ] File `.env` nằm trong `.gitignore`
- [ ] `.env.example` không chứa giá trị thật
- [ ] Không logging password, token, secret
- [ ] Dùng environment variables cho sensitive config

---

## 6. Session & Cookie

- [ ] Cookie được thiết lập HttpOnly
- [ ] Cookie được thiết lập Secure (production)
- [ ] Cookie được thiết lập SameSite
- [ ] Session timeout hợp lý
- [ ] Invalidate session khi logout
- [ ] Session fixation prevention

---

## 7. Output Security

- [ ] Escape output (chống XSS)
- [ ] Set Content-Security-Policy header
- [ ] Set X-Frame-Options header
- [ ] Set X-Content-Type-Options header
- [ ] Set Strict-Transport-Security (HTTPS)
- [ ] CORS config đúng origin

---

## 8. Error Handling

- [ ] Global exception handler
- [ ] Không leak stack trace ra client
- [ ] HTTP status code đúng (401, 403, 404, 500)
- [ ] Error message không lộ internal info
- [ ] Validation error trả về field cụ thể

---

## 9. Dependencies

- [ ] Frontend dependencies không có known vulnerability
- [ ] Backend dependencies không có known vulnerability
- [ ] Dùng version cố định hoặc lock file
- [ ] Định kỳ update dependencies

---

## 10. Checklist per Module

### Auth (P0)
- [ ] Tất cả mục Authentication (Section 1)
- [ ] Tất cả mục Secrets (Section 5)
- [ ] Session & Cookie (Section 6)
- [ ] Error Handling (Section 8)

### Community (P1)
- [ ] Tất cả mục API Security (Section 2)
- [ ] Output Security (Section 7) — chống XSS
- [ ] Validate input cho post/comment
- [ ] Ownership check cho edit/delete

### Marketplace (P1)
- [ ] Tất cả mục API Security (Section 2)
- [ ] Business logic validation (giá, số lượng)
- [ ] Ownership check cho edit/delete
- [ ] Parameter tampering prevention

### Lost & Found (P1)
- [ ] Tất cả mục API Security (Section 2)
- [ ] Ownership check cho edit/delete
- [ ] Status lifecycle validation
- [ ] File upload (nếu có) — Section 4

### File Upload (P2)
- [ ] Tất cả mục File Upload (Section 4)
- [ ] MIME + magic-byte + extension validation
- [ ] Storage isolation
- [ ] Path traversal prevention

---

## Reference

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [OWASP API Security Top 10](https://owasp.org/API-Security/)
- [CWE/SANS Top 25](https://cwe.mitre.org/top25/)
