# SE-002 — CI Security Scanning (CampusHub)

> **Phiên bản:** v2.2.0 | **Ngày:** 2026-09-21
> **Nhánh triển khai:** `test/ci-se-002`
> **Nhánh CI:** `develop` / `main` (`.github/workflows/ci.yml`)
> **Định dạng:** Markdown thuần — đọc được ở mọi nơi, không cần ứng dụng chuyên dụng.

---

## 1. Introduction / Objective

**SE-002** là task bổ sung các kiểm tra bảo mật tự động (security baseline) vào pipeline
**GitHub Actions** của **CampusHub**, nhằm phát hiện sớm các vấn đề bảo mật ngay từ giai
đoạn **Pull Request** — trước khi code được merge vào `develop`/`main`.

### Mục tiêu

* Đưa **3 loại kiểm tra bảo mật** vào CI: dependency vulnerability, secret leakage, SAST.
* Tự động làm **fail pipeline** khi phát hiện rủi ro mức cao, giúp team xử lý ngay.
* **Không** thay đổi bất kỳ logic/chức năng nào của ứng dụng.

### Phạm vi

```text
[✓] Thêm CI security checks               (.github/workflows/ci.yml)
[✓] Cấu hình công cụ scan phụ trợ         (.gitleaks.toml, backend/suppression.xml)
[ ] Thay đổi application code             (KHÔNG — ngoài phạm vi)
```

### Vấn đề cần giải quyết

| Vấn đề                     | Mô tả                                           | Kiểm tra xử lý         |
| -------------------------- | ----------------------------------------------- | ---------------------- |
| Dependency vulnerabilities | Thư viện có CVE, gói nổi tiếng bị lỗ hổng       | `npm audit` + OWASP DC |
| Secret leakage             | API key, token, mật khẩu lọt vào repo/lịch sử   | Gitleaks               |
| SAST / security issues     | Lỗi logic bảo mật trong mã nguồn (SQLi, XSS...) | Semgrep                |

---

## 2. Current Problem

### Trạng thái trước khi triển khai SE-002

```text
Before SE-002
        │
        ├── Frontend
        │     └── Chưa có automated dependency security check
        │
        ├── Backend
        │     └── Chưa có automated dependency vulnerability scan
        │
        ├── Repository
        │     └── Chưa có secret scanning
        │
        └── Source Code
              └── Chưa có SAST
```

### Vì sao cần CI security scanning?

* **Manual check không bền:** con người quên kiểm tra, thư viện mới cập nhật mỗi ngày
  (NVD ghi nhận lỗ hổng mới liên tục), một `npm install`/`mvn dependency:tree` mới có thể
  kéo theo dependency nhiễm CVE ngay lúc đó.
* **Phát hiện trễ = chi phí cao:** lỗ hổng dependency phát hiện sau khi merge và release
  sẽ đắt hơn nhiều so với chặn ngay tại PR.
* **Secret cần phát hiện tự động:** secret lỡ commit (kể cả commit cũ trong lịch sử) khó
  dò bằng mắt; cần tool quét toàn bộ lịch sử git.
* **CI tạo security gate có bằng chứng:** khi pipeline fail, team thấy kết quả ngay ở PR;
  kiểm tra diễn ra **tự động, lặp lại, có log/report**.

---

## 3. Security Requirements

| Requirement              | Tool                   | Mục đích                                           |
| ------------------------ | ---------------------- | -------------------------------------------------- |
| Frontend dependency scan | `npm audit`            | Phát hiện dependency vulnerability (high/critical) |
| Backend dependency scan  | OWASP Dependency-Check | Phát hiện CVE trong Java dependencies              |
| Secret scanning          | Gitleaks               | Phát hiện API key, password, token...              |
| SAST                     | Semgrep                | Phát hiện security issue trong source code         |

### Trigger

```text
[✓] Pull Request vào develop/main   (on: pull_request)
[✓] Push lên develop/main           (on: push)
[ ] Scheduled scan                  (chưa bật)
```

Các job đánh dấu `if: github.event_name != 'schedule'` — guard chuẩn bị sẵn cho việc
thêm lịch chạy `schedule` sau này, **hiện chưa có** trigger định kỳ (nightly) được bật.

### Permission & concurrency

```text
permissions:
  contents: read          # nguyên tắc least privilege

concurrency:
  group: ci-<workflow>-<ref>   # cùng workflow + cùng ref chỉ chạy 1 pipeline
  cancel-in-progress: true     # commit/PR mới huỷ pipeline cũ đang chạy
```

Riêng job Secret Scanning được mở thêm `pull-requests: write` để Gitleaks ghi kết quả
scan lên Pull Request (`permissions: contents: read, pull-requests: write`).

---

## 4. CI/CD Security Flow

Các job chạy **độc lập, song song** (GitHub Actions) — sơ đồ dưới đây biểu diễn **các
nhóm kiểm tra**, không phải thứ tự tuần tự vắt qua các job.

```text
                    Git Push / Pull Request
                              │
                              ▼
                    ┌─────────────────┐
                    │ GitHub Actions  │
                    └────────┬────────┘
                             │
        ┌──────────┬─────────┼─────────┬──────────┐
        ▼          ▼         ▼         ▼          ▼
   frontend   backend    backend-   secret-    sast-
                         dependency- scan      scan
                         scan
   (npm ci,   (mvn      (NVD cache, (Gitleaks, (Semgrep,
    lint,      verify +  DC check,   full       4 rule
    build,     MySQL     report)     history)   sets)
    npm audit) service)
        │          │         │         │          │
        └──────────┴────┬────┴─────────┴──────────┘
                        ▼
                 GitHub Actions Result
                        │
              ┌─────────┴─────────┐
              ▼                   ▼
           SUCCESS               FAILURE
              │                   │
              ▼                   ▼
          CI passes        PR/CI marked failed
                              │
                              ▼
                    Merge blocking depends on
                    branch protection / ruleset
```

Trong sơ đồ trên, **5 nhánh là 5 job độc lập, chạy song song** trong `ci.yml` — không phải
luồng tuần tự; kết quả của mỗi job gộp lại thành kết quả chung của pipeline.

**Lưu ý:** job fail sẽ làm PR/CI được đánh dấu **failed**. Việc trạng thái failed có thực sự
**chặn merge** hay không phụ thuộc vào **branch protection/ruleset** được cấu hình cho
`develop`/`main`; CI tự nó không đảm bảo branch được bảo vệ nếu repository chưa yêu cầu
status check tương ứng.

### Thành phần cụ thể trong pipeline

| Job                       | Công cụ / action                                                                            | Cấu hình chính                                           |
| ------------------------- | ------------------------------------------------------------------------------------------- | -------------------------------------------------------- |
| `frontend`                | `checkout@v6`, `setup-node@v7` (Node 24)                                                    | `npm ci` → lint → build → `npm audit --audit-level=high` |
| `backend`                 | `checkout@v6`, `setup-java@v6` (Java 21 Temurin) + MySQL 8.4                                | `mvn --batch-mode verify --file backend/pom.xml`         |
| `backend-dependency-scan` | `setup-java@v6`, `cache/restore@v4`, `cache/save@v4`, `upload-artifact@v4`, plugin `12.1.0` | `update-only` → `check` (`failBuildOnCVSS=7`, NVD cache) |
| `secret-scan`             | `checkout@v6` (`fetch-depth: 0`), `gitleaks-action@v2`                                      | full history + `.gitleaks.toml`                          |
| `sast-scan`               | container `semgrep/semgrep:1.86.0`                                                          | 4 rule sets + `--error --severity ERROR`                 |

---

## 5. Implementation

### 5.1 Frontend

Chức năng: cài đúng dependency theo lockfile → kiểm tra style → build → quét lỗ hổng.
Toàn bộ job chạy trong `frontend/` (qua `defaults.run.working-directory`).

```text
npm ci
   ↓
npm run lint
   ↓
npm run build
   ↓
npm audit --audit-level=high
```

| Bước                           | Mục đích                                                           |
| ------------------------------ | ------------------------------------------------------------------ |
| `npm ci`                       | Cài dependency chính xác theo `package-lock.json` (deterministic). |
| `npm run lint`                 | Bắt lỗi coding style sớm.                                          |
| `npm run build`                | Đảm bảo frontend build được (Vite/TypeScript).                     |
| `npm audit --audit-level=high` | Phát hiện dependency có lỗ hổng **high/critical**; có thì CI fail. |

Ngoài ra `setup-node@v7` còn dùng `cache: npm` với
`cache-dependency-path: frontend/package-lock.json` để tái sử dụng npm cache giữa các lần chạy.

### 5.2 Backend

Chức năng: chạy test/build backend với database test.

```text
MySQL service
      ↓
Maven verify
      ↓
PASS / FAIL
```

Cấu hình job `backend` trong `ci.yml`:

* **Service MySQL 8.4** (`services.mysql`): `MYSQL_ROOT_PASSWORD: root`,
  `MYSQL_DATABASE: campushub`, expose port `3306`, kèm health check bằng
  `mysqladmin ping` (interval 10s, timeout 5s, retries 10) để job chờ DB sẵn sàng.
* **Env** `DB_USERNAME` / `DB_PASSWORD` = `root` — chỉ là credential của **test
  container** trong CI, không phải secret production.
* **Maven build/test:** `mvn --batch-mode verify --file backend/pom.xml` (Java 21 Temurin,
  cache Maven qua `cache-dependency-path: backend/pom.xml`).
* **Kết quả:** job `backend` pass nếu `mvn verify` thành công và fail nếu Maven trả về
  exit code khác 0.

> **Phân tách:** OWASP Dependency-Check là một job độc lập `backend-dependency-scan`,
> được mô tả tại mục **5.3**, không nằm trong chuỗi `Maven verify` của job `backend`.

### 5.3 OWASP Dependency-Check

Chức năng: so khớp dependency Maven với NVD để tìm CVE; fail khi lỗ hổng đạt ngưỡng.

```text
Restore NVD data cache
      ↓
Update NVD database
      ↓
Save NVD data cache
      ↓
Dependency-Check
      ↓
CVSS >= 7 ?
      ├── Yes → CI failure
      └── No  → Upload dependency-check report
```

Các điểm cấu hình quan trọng (job `backend-dependency-scan`):

* **`backend/suppression.xml`** — loại trừ các finding đã được xác minh là false positive
  hoặc đang được theo dõi theo quyết định quản lý rủi ro (xem 5.6).
* **`-DfailBuildOnCVSS=7`** — build fail nếu có 1+ lỗ hổng **CVSS >= 7**.
* **`-DknownExploitedEnabled=false`** — không bật danh sách CISA KEV ở lần chạy hiện tại.
* **`-DossindexAnalyzerEnabled=false`** — tắt analyzer bên thứ ba (Sonatype OSS Index);
  scan hiện tại không sử dụng nguồn này.
* **`-DautoUpdate=false`** — lần chạy `check` chính **không tự** tải data mới; data được
  chuẩn bị riêng ở bước `update-only`.
* **`-DnvdApiKeyEnvironmentVariable=NVD_API_KEY`** — lấy API key qua biến môi trường,
  không hardcode.
* **`-Dformat=HTML`** — xuất report `backend/target/dependency-check-report.html`, tải lên
  dưới tên artifact `dependency-check-report`.
* **NVD cache** — `.odc-data` được restore/save bằng
  `actions/cache/restore@v4` / `actions/cache/save@v4`; cache được dùng để giảm thời gian
  tải lại dữ liệu NVD.
* Bước `update-only` chạy với `MAVEN_OPTS: -Xmx3g` để cấp đủ heap khi cập nhật database.

### 5.4 Gitleaks

Chức năng: quét **toàn bộ lịch sử git** để tìm secret đã/đang tồn tại.

```text
Git repository
      ↓
Full Git history
      ↓
Gitleaks
      ↓
Secret detected?
      ├── No  → PASS
      └── Yes → FAIL
```

Cấu hình (job `secret-scan`):

* `actions/checkout@v6` với **`fetch-depth: 0`** — clone full history, không lấy diff duy nhất.
* `GITLEAKS_CONFIG: .gitleaks.toml` + `GITHUB_TOKEN` (default token, tự sinh).
* Quyền: `contents: read` + `pull-requests: write` (ghi kết quả lên PR).

File `.gitleaks.toml`:

```toml
title = "CampusHub Gitleaks Configuration"

[extend]
useDefault = true

[allowlist]
description = "Allowlist for mock/test credentials and example environment files only"
paths = [
  '''backend/src/test/.*''',
  '''frontend/\.env\.example''',
  '''backend/\.env\.example'''
]
```

> ⚠️ **Điểm chưa chặt:** pattern `backend/src/test/.*` phủ **toàn bộ** thư mục test.
> Bất kỳ file test mới nào cũng được miễn quét. Đây là một giới hạn cần được thu hẹp
> trong một iteration bảo mật tiếp theo.

### 5.5 SAST — Semgrep

Chức năng: SAST trên mã nguồn Java & TypeScript/React bằng rule sets bảo mật.

```text
Source code
     ↓
Semgrep rules
     ↓
Security findings
     ↓
ERROR severity
     ↓
CI result
```

Cấu hình (job `sast-scan`):

* Chạy trong container `semgrep/semgrep:1.86.0` cho môi trường ổn định.
* Rule sets: `p/security-audit`, `p/java`, `p/javascript`, `p/owasp-top-ten`.
* `--error --severity ERROR` — **chỉ các finding mức ERROR mới làm job fail**.
* Điều này giúp giảm false positive/noise ở các mức thấp hơn, nhưng đồng thời có nghĩa
  SAST hiện tại **không bắt buộc xử lý WARNING**.

### 5.6 Suppression hiện tại (backend)

4 CVE trong `backend/suppression.xml`:

| CVE                       | Gói                                           | Chi tiết từ suppression.xml                                                                                                                                                                                                                                              | Owner                            | Hạn xử lý                                                         | Đánh giá khả năng khai thác                                                                                                                                                         |
| ------------------------- | --------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------- | ----------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| CVE-2026-60586 (CVSS 7.7) | `com.mysql:mysql-connector-j`                 | Chưa có bản vá chính thức theo trạng thái được ghi nhận ngày 2026-09-12; ảnh hưởng 9.7.0–9.7.1; dependency trực tiếp, được quản lý bởi Spring Boot BOM; xác nhận qua `mvn dependency:tree`.                                                                              | **TBD — cần gán** (backend lead) | Trước next release; tái kiểm tra mỗi Oracle Critical Patch Update | **Chưa đánh giá được.** Suppression chỉ loại finding khỏi kết quả fail của Dependency-Check; không chứng minh dependency an toàn và không loại bỏ lỗ hổng khỏi application/runtime. |
| CVE-2026-60623 (CVSS 7.1) | `com.mysql:mysql-connector-j`                 | Cùng trạng thái và chu kỳ với CVE-2026-60586; ảnh hưởng 9.7.0–9.7.1.                                                                                                                                                                                                     | **TBD — cần gán** (backend lead) | Trước next release; tái kiểm tra mỗi Oracle Critical Patch Update | **Chưa đánh giá được.** Suppression chỉ loại finding khỏi kết quả fail của Dependency-Check; không chứng minh dependency an toàn và không loại bỏ lỗ hổng khỏi application/runtime. |
| CVE-2025-7962             | `org.eclipse.angus:angus-activation`          | Dependency-Check gắn nhầm với `cpe:2.3:a:eclipse:angus_mail`. Theo GHSA-9342-92gg-6v29, CVE này chỉ ảnh hưởng `com.sun.mail:jakarta.mail` và `org.eclipse.angus:smtp`; dự án chỉ có `angus-activation:2.0.3`, không có `jakarta.mail`/`angus-smtp`. Verified 2026-09-13. | Không áp dụng                    | Không áp dụng (giữ suppress)                                      | **False positive** — đã xác minh qua dependency tree.                                                                                                                               |
| CVE-2025-15104            | `org.hibernate.validator:hibernate-validator` | Dependency-Check gắn nhầm với `cpe:2.3:a:validator:validator`. Theo NVD, CVE mô tả SSRF trong Nu Html Checker (`validator.nu`) — sản phẩm không liên quan. Dự án dùng `hibernate-validator:9.1.3.Final`, không có `vnu-jar`. Verified 2026-09-13.                        | Không áp dụng                    | Không áp dụng (giữ suppress)                                      | **False positive** — đã xác minh qua dependency tree.                                                                                                                               |

> ⚠️ **Owner của 2 CVE `mysql-connector-j` chưa được gán.** Trước khi release, bắt buộc
> gán người chịu trách nhiệm, chốt ngày tái đánh giá và xác nhận trạng thái bản vá để
> quyết định tiếp tục suppression hay nâng cấp dependency.

---

## 6. Rationale chọn công cụ

| Công cụ                | Lý do sử dụng                                                                                          | Giới hạn cần lưu ý                                                                                  |
| ---------------------- | ------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------- |
| `npm audit`            | Tích hợp trực tiếp với npm ecosystem và `package-lock.json`; phù hợp làm dependency gate cho frontend. | Phụ thuộc dữ liệu advisory của npm; không thay thế SAST hoặc secret scanning.                       |
| OWASP Dependency-Check | Phù hợp với Maven/Java và đối chiếu dependency với NVD; có HTML report và suppression mechanism.       | Có thể phát sinh false positive; độ đầy đủ/phạm vi phụ thuộc dữ liệu vulnerability source được bật. |
| Gitleaks               | Chuyên dụng cho phát hiện secret trong Git và có thể quét full history.                                | Allowlist rộng có thể bỏ sót secret nếu secret nằm trong phạm vi được miễn.                         |
| Semgrep                | SAST nhanh, có rule set cho Java/JavaScript và OWASP security patterns.                                | Rule-based scanning chỉ phát hiện những pattern mà rule bao phủ; cấu hình hiện chỉ fail ở ERROR.    |

Các công cụ được dùng bổ sung cho nhau, không coi một công cụ là thay thế hoàn toàn cho
các lớp kiểm tra còn lại.

---

## 7. Security Controls

Giải thích vì sao từng option trong YAML tồn tại:

| Configuration                           | Purpose                                                                |
| --------------------------------------- | ---------------------------------------------------------------------- |
| `permissions: contents: read`           | Least privilege — không leo thang quyền mặc định.                      |
| `pull-requests: write` (secret-scan)    | Đủ quyền để Gitleaks ghi kết quả lên PR.                               |
| `concurrency + cancel-in-progress`      | Cùng ref chỉ 1 pipeline chạy; commit/PR mới huỷ bản cũ.                |
| `if: github.event_name != 'schedule'`   | Guard sẵn cho trigger định kỳ trong tương lai; hiện chưa bật schedule. |
| `NVD_API_KEY` (secrets)                 | Xác thực NVD API, tăng giới hạn truy cập dữ liệu NVD.                  |
| `dataDirectory` (`.odc-data`)           | Tách NVD database khỏi Maven cache.                                    |
| GitHub Actions cache                    | Giảm việc tải lại NVD data mỗi lần chạy.                               |
| `failBuildOnCVSS=7`                     | Fail khi vulnerability đạt CVSS >= 7.                                  |
| `fetch-depth: 0`                        | Gitleaks kiểm tra **toàn bộ** lịch sử commit.                          |
| `--severity ERROR` (Semgrep)            | Chỉ fail với finding mức ERROR.                                        |
| `if-no-files-found: warn`               | Không làm workflow fail chỉ vì report không tồn tại.                   |
| `setup-node@v7` / `setup-java@v6` cache | Tận dụng npm / Maven cache giữa các lần chạy.                          |
| `GITLEAKS_CONFIG` + allowlist paths     | Cấu hình phạm vi quét và loại trừ test/example hiện tại.               |

---

## 8. Limitations

Các giới hạn dưới đây là một phần của trạng thái bảo mật hiện tại và cần được xem xét
khi đánh giá mức độ bao phủ của SE-002.

### 8.1 Dependency-Check có thể có false positive

OWASP Dependency-Check thực hiện matching giữa dependency và thông tin vulnerability/CPE.
Việc matching có thể tạo **false positive**, như các trường hợp đã được xác minh trong
`backend/suppression.xml`.

Vì vậy, suppression phải được xem là **quyết định có kiểm soát**, không phải bằng chứng
rằng package hoàn toàn an toàn. Mỗi suppression cần có lý do, phạm vi và thời điểm
re-evaluate rõ ràng.

### 8.2 SAST chỉ bao phủ một phần lỗi

Semgrep là phương pháp **rule-based static analysis**. Việc sử dụng các rule set:

```text
p/security-audit
p/java
p/javascript
p/owasp-top-ten
```

không đồng nghĩa với việc toàn bộ lỗi bảo mật trong source code đều được phát hiện.

Đặc biệt, cấu hình hiện tại sử dụng:

```text
--error
--severity ERROR
```

nên các finding ở mức `WARNING` không làm job fail. Điều này làm giảm noise nhưng cũng
làm giảm phạm vi security gate.

### 8.3 Phụ thuộc nguồn dữ liệu NVD bên ngoài

OWASP Dependency-Check phụ thuộc vào dữ liệu vulnerability từ **NVD** và khả năng truy
cập nguồn dữ liệu này.

Nếu NVD API/data source gặp lỗi, rate limit, delay hoặc dữ liệu chưa được cập nhật,
kết quả scan có thể bị ảnh hưởng.

`NVD_API_KEY` và cache giúp cải thiện khả năng cập nhật dữ liệu, nhưng **không loại bỏ
dependency vào NVD**.

### 8.4 `--severity ERROR` bỏ qua WARNING của Semgrep

Security gate hiện tại chỉ fail đối với Semgrep finding mức `ERROR`.

Do đó:

```text
ERROR   → CI failure
WARNING → không làm CI failure
```

Một finding WARNING vẫn có thể cần được review thủ công. Không nên diễn giải
`CI PASS` thành “không có vấn đề bảo mật”.

### 8.5 KEV và OSS Index đang tắt

Hiện tại Dependency-Check sử dụng:

```text
-DknownExploitedEnabled=false
-DossindexAnalyzerEnabled=false
```

Do đó pipeline **không sử dụng CISA Known Exploited Vulnerabilities (KEV)** và
**không sử dụng Sonatype OSS Index** trong lần scan hiện tại.

Điều này làm giảm số nguồn/thông tin bổ sung được dùng để đánh giá dependency risk.

### 8.6 Allowlist test còn rộng

Gitleaks đang allowlist:

```text
backend/src/test/.*
```

Điều này miễn toàn bộ thư mục test khỏi secret scanning.

Nếu một secret thật vô tình được đưa vào file bên trong thư mục này, Gitleaks có thể không
phát hiện. Đây là một điểm cần thu hẹp trong iteration sau.

### 8.7 GitHub Actions chưa pin theo SHA

Các action hiện đang tham chiếu theo version/tag, ví dụ:

```text
actions/checkout@v6
actions/setup-node@v7
actions/setup-java@v6
actions/cache/restore@v4
actions/cache/save@v4
actions/upload-artifact@v4
gitleaks/gitleaks-action@v2
```

Chưa pin action về **commit SHA bất biến**.

Do đó supply-chain integrity của CI chưa ở mức chặt nhất; tag/action reference có thể thay
đổi theo upstream.

### 8.8 Chưa có scan định kỳ

Workflow hiện chỉ có:

```text
pull_request
push
```

Chưa bật:

```yaml
schedule:
```

Vì vậy nếu dependency/source **không thay đổi**, pipeline không tự động chạy lại hàng ngày
để phát hiện vulnerability mới xuất hiện trong NVD/advisory sau thời điểm commit.

### 8.9 Suppression không loại bỏ vulnerability khỏi application

Đặc biệt đối với hai CVE của `mysql-connector-j`, suppression chỉ ảnh hưởng tới kết quả
của Dependency-Check:

```text
Dependency vẫn tồn tại
        ↓
Dependency vẫn nằm trong application
        ↓
Suppression chỉ làm finding không trigger gate theo cấu hình hiện tại
```

Do đó suppression **không phải remediation**. Khi có bản vá phù hợp, dependency cần được
nâng cấp và suppression cần được xem xét/gỡ bỏ.

---

## 9. Cách xử lý khi CI fail

Khi một security job fail, không nên chỉ rerun CI để làm mất trạng thái đỏ. Cần xác định
nguyên nhân trước.

```text
CI FAILED
    ↓
Xác định job fail
    ↓
Đọc log / artifact
    ↓
Phân loại finding
    ├── True positive
    │      ↓
    │   Remediate
    │   (upgrade/fix/remove secret/fix code)
    │
    ├── False positive
    │      ↓
    │   Verify bằng evidence
    │   ↓
    │   Suppression có lý do + owner + re-evaluation date
    │
    └── Tool / infrastructure issue
           ↓
        Fix CI/data source
```

### Dependency vulnerability

1. Xác định CVE, package và version bị ảnh hưởng.
2. Kiểm tra dependency tree để xác định dependency trực tiếp hay transitive.
3. Ưu tiên nâng cấp lên version đã được sửa.
4. Nếu chưa có bản vá, phải ghi nhận owner, lý do và ngày tái đánh giá.
5. Không dùng suppression chỉ để biến CI từ fail thành pass mà không có đánh giá.

### Secret detected

1. Xác định secret đã bị lộ ở đâu.
2. **Revoke/rotate secret** nếu secret có khả năng còn hợp lệ.
3. Kiểm tra Git history nếu secret từng được commit.
4. Sau khi remediation mới xử lý finding/allowlist nếu thực sự cần.

### Semgrep finding

1. Đọc rule và location của finding.
2. Xác minh có phải true positive hay false positive.
3. Với true positive, sửa source code và thêm test nếu phù hợp.
4. Với false positive, ghi nhận bằng chứng thay vì chỉ bỏ qua kết quả.

### CI/tool failure

Nếu lỗi đến từ NVD, network, cache, runner hoặc dependency của CI tool, cần phân biệt
**security finding** với **CI infrastructure failure** trước khi quyết định rerun.

---

## 10. Testing / Verification

CI đã chạy **SUCCESS** trên nhánh `test/ci-se-002` — bằng chứng theo từng nhóm kiểm tra:

### Test 1 — Normal CI

* GitHub Actions workflow: `CampusHub CI` chạy toàn bộ jobs trên mỗi PR/push.
* Link theo dõi:
  `https://github.com/TranHuuQuyet/CampusHub/actions?query=branch:test/ci-se-002`
* Trạng thái: **Success** cho chuỗi job `frontend`, `backend`,
  `backend-dependency-scan`, `secret-scan`, `sast-scan`.

### Test 2 — Frontend dependency scan

```text
npm audit --audit-level=high
```

* Không còn lỗ hổng high/critical trong `package-lock.json` → job `frontend` pass.

### Test 3 — Backend Dependency-Check

```text
Backend Dependency Scan (OWASP Dependency-Check)
        ✓
```

* `failBuildOnCVSS=7` không tìm thấy lỗ hổng >= 7 ngoài các finding đã suppress theo
  cấu hình hiện tại.
* Report tải lên `dependency-check-report`.

### Test 4 — Gitleaks

```text
Secret Scanning (Gitleaks)
        ✓
```

* Quét full history `fetch-depth: 0`, không phát hiện secret ngoài allowlist.

### Test 5 — Semgrep

```text
SAST (Semgrep)
        ✓
```

* 4 rule sets chạy `--error --severity ERROR`, không có finding mức ERROR.

### Test 6 — NVD cache

```text
Restore NVD data cache
        ↓
Update NVD database (update-only + NVD_API_KEY)
        ↓
Save NVD data cache
```

* Cache restore/save pass cho job `backend-dependency-scan`.

> Nếu muốn có bằng chứng trực quan (screenshot), chụp mục **Summary** của run qua link
> trên hoặc log từng job; nội dung doc này ghi lại trạng thái kết quả.

---

## 11. Failure Scenario

Chứng minh CI **không chỉ chạy cho có** — khi phát hiện rủi ro, security job có thể fail:

```text
Security issue detected
        ↓
Tool returns non-zero
        ↓
GitHub Actions job fails
        ↓
PR CI = FAILED
        ↓
Merge blocked?
        │
        └── Depends on branch protection / ruleset
```

| Scenario                                             | Expected result |
| ---------------------------------------------------- | --------------- |
| High/Critical dependency vulnerability (`npm audit`) | FAIL            |
| Backend vulnerability CVSS >= 7                      | FAIL            |
| Secret detected (Gitleaks)                           | FAIL            |
| Semgrep finding mức ERROR                            | FAIL            |

Không cố tạo vulnerability thật cho project; hành vi dựa trên cấu hình:

* `npm audit --audit-level=high` → exit code khác 0 khi có high/critical.
* `-DfailBuildOnCVSS=7` → Maven build fail khi CVSS đạt ngưỡng.
* Gitleaks exit code khác 0 khi match secret ngoài allowlist.
* `semgrep --error --severity ERROR` → exit code khác 0 khi có finding ERROR.

> **Quan trọng:** `FAIL` ở đây trước hết có nghĩa là **CI job/workflow failed**. Việc PR
> có bị **chặn merge** hay không phụ thuộc repository đã cấu hình branch protection/ruleset
> yêu cầu status check tương ứng hay chưa.

---

## 12. Final Result

| Security area            | Status           |
| ------------------------ | ---------------- |
| Frontend dependency scan | ✅ Implemented    |
| Backend dependency scan  | ✅ Implemented    |
| NVD cache                | ✅ Implemented    |
| Secret scanning          | ✅ Implemented    |
| SAST                     | ✅ Implemented    |
| PR/Push trigger          | ✅ Implemented    |
| Scheduled scan (nightly) | ❌ Chưa bật       |
| Application code changed | ❌ No             |
| Action pinning theo SHA  | ❌ Chưa thực hiện |
| CISA KEV analyzer        | ⚠️ Disabled      |
| OSS Index analyzer       | ⚠️ Disabled      |

> **Kết luận:** SE-002 đã triển khai baseline security scanning gồm dependency scanning,
> secret scanning và SAST trong CI. Pipeline hiện chạy trên PR/push vào `develop`/`main`,
> có report và các security gate theo cấu hình hiện tại.
>
> Tuy nhiên, **CI PASS không đồng nghĩa với việc application không có vulnerability**.
> Phạm vi phát hiện bị giới hạn bởi nguồn dữ liệu, rule coverage, severity threshold,
> suppression/allowlist và các analyzer đang disabled.
>
> Các hướng cải thiện tiếp theo gồm:
>
> 1. Thu hẹp Gitleaks allowlist.
> 2. Pin GitHub Actions theo commit SHA.
> 3. Đánh giá lại việc bật CISA KEV/OSS Index.
> 4. Bật scheduled scan.
> 5. Review các Semgrep WARNING thay vì chỉ gate ở ERROR.
> 6. Theo dõi và xử lý hai CVE `mysql-connector-j` khi có advisory/bản vá phù hợp.
> 7. Gỡ suppression ngay khi false positive được giải quyết hoặc dependency được nâng cấp.

---

*Tài liệu liên quan: `docs/security/threat-model.md`, `docs/security/security-checklist.md`, `SECURITY.md`.*

