# Reflectly (Aura Self AI) — Tài liệu Hệ thống & Nghiệp vụ (Backend)

> Tài liệu này mô tả **hiện trạng thực tế** của backend, dựa trên việc đọc trực tiếp source code, cấu hình và migration SQL (không phải mô tả kỳ vọng/kế hoạch). Viết theo góc nhìn Business Analyst — tập trung vào "hệ thống làm gì, dữ liệu gì, quy tắc gì", không đi sâu chi tiết implementation trừ khi cần thiết để hiểu nghiệp vụ.
>
> Tài liệu song song ở frontend: [`reflectly-fe/spec.md`](https://github.com/hankimthuy/reflectly-fe/blob/main/spec.md) (màn hình, luồng UI, tính năng theo trải nghiệm người dùng).
>
> Cách duy trì tài liệu này khi có thay đổi mới: xem [`CLAUDE.md`](./CLAUDE.md).

---

## 1. Tổng quan dịch vụ

Backend của **Reflectly / "Aura Self AI"** — một app self-reflection có AI Coach chat, Bản đồ Mối quan hệ Cá nhân, và nhật ký cá nhân. Package Java: `org.mentorship.reflectly`. Cung cấp REST API cho `reflectly-fe` (React SPA).

Sản phẩm đã trải qua 2 lần pivot (xem chi tiết lịch sử ở `reflectly-fe/spec.md` mục 1.1): từ app nhật ký ẩn dụ khu vườn ("MimoSe") → thêm Energy Tracking + Action Protocol → pivot sang AI Coach + Relationship Map ("Aura Self AI"). Backend hiện tại là **siêu tập** của tất cả các giai đoạn: một số API (Energy Logs, Action Protocols) vẫn tồn tại đầy đủ dù frontend không còn dùng tới (xem mục 8).

---

## 2. Kiến trúc & Tech stack

- **Ngôn ngữ:** Java 21.
- **Framework:** Spring Boot 3.5.6 (Spring Web MVC, Spring Data JPA, Spring Security + OAuth2 Resource Server).
- **Cơ sở dữ liệu:** PostgreSQL (Neon/Supabase ở production; image `pgvector/pgvector:pg16` ở local — đã cài sẵn extension pgvector cho tính năng tìm kiếm ngữ nghĩa **trong tương lai**, hiện chưa dùng tới).
- **ORM:** Hibernate/Spring Data JPA. **Chưa dùng Flyway** — migration được quản lý thủ công qua các file SQL trong `documentation/migrations/` (đã được ghi chú là việc cần làm trước khi có thay đổi schema tiếp theo).
- **Xác thực:** JWT tự phát hành (HS256, thư viện `io.jsonwebtoken`), phát sinh từ 2 luồng đăng nhập: Google OAuth2 (auth-code) hoặc tài khoản/mật khẩu (BCrypt).
- **Tài liệu API:** springdoc-openapi, Swagger UI tại `/swagger-ui.html`.
- **AI:** Google Gemini (SDK `google-genai`) — dùng cho AI Coach chat, tóm tắt hội thoại, và trích xuất bộ nhớ tự động.
- **Giới hạn tần suất:** bucket4j (in-memory, per-IP cho route auth, per-user cho gửi tin nhắn chat).
- **Build & CI:** Maven; CI hiện tại chạy `mvn clean verify -DskipTests` — **không chạy test tự động** dù đã khai báo dependency test (H2, JUnit).
- **Triển khai:** nhánh `main` → Azure Web App "ReflectlyBE" (khu vực Southeast Asia); nhánh `develop` → AWS EC2 (staging).

---

## 3. Dữ liệu — 12 bảng chính

Nguồn sự thật: `db/migration/V1__init_schema.sql` (script tạo schema mới nhất) + các file trong `documentation/migrations/00{1,2,3}-*.sql` (lịch sử thay đổi tăng dần). **Không dùng** `database_schema.sql` ở gốc repo — đây là schema mẫu cũ, chỉ có 4 bảng, không phản ánh hệ thống hiện tại.

Hầu hết các bảng có cột audit chung (`created_date, created_by, last_modified_date, last_modified_by`) do Spring Data JPA tự quản lý.

| Bảng | Ý nghĩa nghiệp vụ | Field/quan hệ đáng chú ý |
|---|---|---|
| **users** | Tài khoản người dùng | email/username unique, password_hash (nullable nếu đăng nhập Google), onboarding_completed |
| **user_core_values** | Giá trị cốt lõi người dùng chọn | quan hệ 1-nhiều với users, mỗi dòng 1 giá trị |
| **entries** | Nhật ký tự viết | title, reflection (text), template_key (thêm sau ở migration 001) |
| **entry_emotions** | Nhãn cảm xúc gắn vào 1 entry | cho phép trùng cảm xúc (không có composite PK) |
| **conversations** | Phiên chat AI Coach | status (ACTIVE/ENDED/EXTRACTING/EXTRACTED/EXTRACTION_FAILED), summary (TEXT, **mã hoá AES-256** khi lưu, thêm ở migration 002) |
| **conversation_messages** | Từng tin nhắn trong phiên chat | role (USER/ASSISTANT), content (TEXT, **mã hoá AES-256** khi lưu; có thể bị xoá trắng sau khi đã trích xuất xong nếu bật chế độ purge) |
| **insights** | Insight do AI tự động trích xuất (chỉ đọc) | category (VALUE/BEHAVIOR_PATTERN/RELATIONSHIP), liên kết tuỳ chọn tới conversation và person (person thêm ở migration 003) |
| **people** | Người trong Bản đồ Mối quan hệ | relationship_type (FAMILY/FRIEND/PARTNER/COLLEAGUE/MANAGER/OTHER), last_mentioned_at |
| **relationship_events** | Sự kiện mối quan hệ do AI trích xuất | event_type (CONFLICT/BONDING/NEUTRAL), sentiment_score (-1..1), gắn với 1 person |
| **action_protocols** | Kịch bản ứng phó tình huống (tính năng cũ, xem mục 8) | trigger_situation, script, usage_count |
| **protocol_usages** | Lịch sử dùng 1 action protocol | effectiveness (WORKED/PARTIAL/DIDNT_WORK) |
| **saved_framework_entries** | "Đúc kết" người dùng tự lưu (Johari Window...) | framework_type (FREEFORM/JOHARI_WINDOW/ACT_MATRIX/PERSONAL_SWOT/LIFE_POSITIONS — chỉ FREEFORM và JOHARI_WINDOW có API/UI dùng tới hiện nay), payload (JSONB, cấu trúc thay đổi theo framework_type) |

**Bảo mật ở tầng dữ liệu:** Row-Level Security (RLS) được bật trên toàn bộ bảng ở Supabase nhưng **không có policy nào** — chủ đích, vì backend Spring Boot kết nối trực tiếp qua JDBC bằng role `postgres` (bỏ qua RLS), mục đích của việc bật RLS chỉ là chặn truy cập public ngoài ý muốn qua cơ chế PostgREST tự động của Supabase.

**Không có bảng "Energy Log" riêng** trong schema hiện tại được liệt kê ở trên dù API vẫn hoạt động — kiểm tra thực thể `EnergyLogEntity` trong code khi cần chi tiết chính xác cột (spec này không liệt kê lại toàn bộ để tránh trùng lặp thông tin dễ lệch pha với migration thực tế).

---

## 4. Danh mục API đầy đủ theo domain

Base path: `/api`. Toàn bộ route yêu cầu JWT hợp lệ (`Authorization: Bearer`), **trừ** các route công khai `/api/auth/**` và các route hệ thống (`/swagger-ui.html`, `/v3/api-docs/**`, `/actuator/health`, `/actuator/info`, `/uploads/**`).

| Domain | Method & Path | Mục đích |
|---|---|---|
| **Auth** | `POST /api/auth/google` | Đổi Google auth-code lấy JWT của hệ thống |
| | `POST /api/auth/login` | Đăng nhập tài khoản/mật khẩu → JWT |
| | `POST /api/auth/signup` | Đăng ký tài khoản/mật khẩu → JWT |
| **Người dùng** | `GET /api/users/profile` | Xem hồ sơ hiện tại |
| | `PUT /api/users/profile` | Sửa tên hiển thị |
| | `PUT /api/users/password` | Đổi mật khẩu |
| | `POST /api/users/avatar` | Tải ảnh đại diện (lưu ổ đĩa cục bộ server, xem mục 7) |
| | `PUT /api/users/onboarding` | Lưu Core Values + danh sách người ban đầu, đánh dấu hoàn thành onboarding |
| **Nhật ký** | `GET /api/entries`, `GET/PUT/DELETE /api/entries/{id}`, `POST /api/entries` | CRUD nhật ký, có phân trang |
| **Năng lượng (Energy Logs)** | `GET /api/energy-logs?contextTag=`, `GET /api/energy-logs/range?days=`, `POST /api/energy-logs`, `DELETE /api/energy-logs/{id}` | CRUD + truy vấn xu hướng theo ngày — **xem mục 8, frontend không còn dùng** |
| **Mối quan hệ** | `GET/POST /api/people`, `GET/PUT /api/people/{id}` | Quản lý người trong PRM; `GET` trả kèm `healthSignal` tính toán và `nudgeText` tuỳ chọn |
| **Insight** | `GET /api/insights?personId=` | Danh sách insight AI trích xuất, phân trang, mới nhất trước |
| **Action Protocol** | `GET/POST /api/protocols`, `GET/PUT/DELETE /api/protocols/{id}`, `POST /api/protocols/{id}/use` | CRUD kịch bản ứng phó + ghi nhận lượt dùng và đánh giá hiệu quả — **xem mục 8, frontend không còn dùng** |
| **Đúc kết (Saved Framework Entries)** | `GET/POST /api/saved-framework-entries`, `GET/PUT/DELETE /api/saved-framework-entries/{id}` | CRUD, lọc theo `frameworkType` |
| **Trò chuyện AI Coach** | `POST /api/conversations` | Bắt đầu phiên chat mới (**bị giới hạn quota** — mặc định tối đa 5 phiên/người dùng, kiểm soát chi phí Gemini) |
| | `GET /api/conversations`, `GET /api/conversations/{id}` | Danh sách / chi tiết 1 phiên (kèm toàn bộ tin nhắn) |
| | `POST /api/conversations/{id}/messages` | Gửi tin nhắn, nhận phản hồi AI (**bị giới hạn tần suất theo người dùng**) |
| | `POST /api/conversations/{id}/end` | Kết thúc phiên → kích hoạt trích xuất bộ nhớ bất đồng bộ (xem mục 5) |
| | `POST /api/conversations/{id}/summarize` | Sinh/làm mới bản tóm tắt markdown |

---

## 5. Business logic — nhóm AI Coach (phần phức tạp nhất hệ thống)

Đây là phần nghiệp vụ mới nhất và quan trọng nhất, **không được mô tả trong tài liệu PRD cũ** (`documentation/00-Product-Context/`) vì được xây dựng sau khi tài liệu đó viết:

1. **Chat thời gian thực (`CoachAgentService`):** khi người dùng gửi tin nhắn, hệ thống gọi Gemini (model `gemini-3.6-flash`) với một system prompt tiếng Việt quy định vai trò "huấn luyện Socratic" — phản chiếu câu hỏi lại cho người dùng tự suy ngẫm, **không chẩn đoán, không trị liệu, nếu phát hiện khủng hoảng thì hướng dẫn tìm chuyên gia** — cá nhân hoá theo Core Values của người dùng.
2. **Kết thúc phiên → trích xuất bộ nhớ (`MemoryExtractionService` + `MemoryExtractionPersister`):** khi người dùng bấm "Kết thúc phiên", một sự kiện được phát ra sau khi transaction commit thành công, xử lý **bất đồng bộ** (thread pool riêng `memoryExtractionExecutor`): gọi Gemini (yêu cầu trả về JSON có cấu trúc) để tự động trích xuất ra: người được nhắc tới (Person), sự kiện mối quan hệ (RelationshipEvent), và insight (Insight) — rồi ghi vào database. Có thể tuỳ chọn xoá nội dung tin nhắn gốc sau khi trích xuất xong (data minimization).
3. **Tóm tắt theo yêu cầu (`ConversationSummaryService`):** sinh bản tóm tắt markdown cho 1 phiên, dùng model nhẹ hơn (`gemini-3.5-flash-lite`), có thể gọi lại nhiều lần để làm mới.

Đây chính là cơ chế đứng sau tính năng "Insight Timeline" và một phần của "Bản đồ Mối quan hệ" ở frontend — insight/person không phải lúc nào cũng do người dùng tự tạo mà phần lớn được AI tự động sinh ra sau mỗi phiên chat.

---

## 6. Xác thực & Bảo mật

- **2 luồng đăng nhập hội tụ về 1 loại JWT** do backend tự phát hành (HS256, mặc định hết hạn sau 24h):
  1. **Google OAuth (auth-code flow):** frontend lấy auth-code → backend đổi lấy token Google → xác thực ID token → tìm hoặc tạo user theo email (cập nhật tên/ảnh nếu có thay đổi) → phát JWT riêng.
  2. **Tài khoản/mật khẩu:** mật khẩu băm bằng BCrypt. **Có công tắc bật/tắt theo môi trường** (`app.auth.local-credentials-enabled`) — **mặc định BẬT ở dev, mặc định TẮT ở production** (chỉ Google login được dùng ở production, tận dụng khả năng chống lạm dụng sẵn có của Google). Gọi API đăng nhập bằng mật khẩu khi bị tắt sẽ trả lỗi 403.
- **Không có hệ thống phân quyền/role** — mọi người dùng đã xác thực có quyền truy cập ngang nhau, chỉ giới hạn trong phạm vi dữ liệu của chính họ (mọi truy vấn đều lọc theo `userId`). Không có tài khoản "admin" thật (chuỗi `"admin"` chỉ dùng làm tên mặc định cho audit log khi không có người dùng xác thực).
- **Phiên làm việc không trạng thái (stateless):** JWT gửi qua header `Authorization: Bearer`, không lưu session server-side.
- **Chống lạm dụng:** giới hạn tần suất theo IP cho các route auth, giới hạn theo người dùng cho việc gửi tin nhắn chat, và hạn mức cứng số phiên chat tối đa/người dùng (mặc định 5) — mục đích kiểm soát chi phí gọi Gemini trong giai đoạn đầu ra mắt.
- **Mã hoá tại chỗ (at-rest):** nội dung tin nhắn chat và bản tóm tắt được mã hoá AES-256-GCM trước khi lưu database (khoá qua biến môi trường `ENCRYPTION_KEY`; nếu chưa cấu hình khoá thì dữ liệu được lưu **không mã hoá** — chỉ phù hợp cho dev, không an toàn cho dữ liệu thật).

---

## 7. Tích hợp bên thứ ba

| Dịch vụ | Mục đích | Ghi chú |
|---|---|---|
| **Google OAuth2** | Đăng nhập/định danh | — |
| **Google Gemini** (`gemini-3.6-flash`, `gemini-3.5-flash-lite`) | AI Coach chat, tóm tắt hội thoại, trích xuất bộ nhớ | Khoá API qua `GEMINI_API_KEY` |
| **Lưu trữ file** | Ảnh đại diện | Lưu **trực tiếp trên ổ đĩa cục bộ server** (`uploads/avatars/`) — **rủi ro đã biết:** sẽ mất dữ liệu khi Azure Web App redeploy; cần chuyển sang Blob Storage trước khi mở rộng quy mô |
| **PostgreSQL hosting** | Neon/Supabase ở production | pgvector cài sẵn cho tính năng tìm kiếm ngữ nghĩa tương lai, **chưa dùng** |

**Chưa tích hợp:** thanh toán/subscription, push notification, gửi email, phân tích hành vi (analytics SDK), lưu trữ đám mây (S3/Blob) cho file.

---

## 8. Tính năng có ở Backend nhưng Frontend KHÔNG dùng

API cho các domain sau vẫn hoạt động đầy đủ (CRUD hoạt động bình thường qua Swagger/Postman) nhưng **không có màn hình frontend nào gọi tới** — thuộc về giai đoạn sản phẩm trước khi pivot sang AI Coach:

- **Energy Logs** (`/api/energy-logs/**`) — theo dõi mức năng lượng theo ngữ cảnh.
- **Action Protocols** (`/api/protocols/**`) — kịch bản ứng phó tình huống lặp lại.

Khi cân nhắc thay đổi 2 domain này, cần xác nhận với chủ sản phẩm liệu có kế hoạch hồi sinh ở frontend hay nên cân nhắc gỡ bỏ hẳn khỏi backend để giảm diện tích bảo trì.

---

## 9. Ghi chú về tài liệu cũ trong repo (KHÔNG dùng làm nguồn sự thật)

Các tài liệu sau mô tả một thiết kế **chưa từng được xây dựng** hoặc đã lỗi thời — giữ lại làm tư liệu lịch sử, không phản ánh hệ thống hiện tại:

- `documentation/01-Database/Data-Dictionary.md`, `documentation/01-Database/Schema-ERD.md` — mô tả một thực thể `SocialConnection`/"Orbit" **chưa từng được xây dựng**; hệ thống thật dùng `PersonEntity` + `RelationshipEventEntity` + `InsightEntity` (do AI Coach sinh ra), khác hẳn thiết kế cũ.
- `documentation/02-API-Specs/Endpoints.md` — mô tả API cũ (`EnergyController`/`OrbitController` tại `/api/v1/...`, bọc response dạng `{success, data, message}`) — thực tế không dùng tiền tố `/api/v1` cũng như envelope này; README repo tự ghi chú "Partially outdated — see Swagger for actual API".
- `documentation/00-Product-Context/PRD-Summary.md`, `Feature-System-Roadmap.md` — tài liệu tầm nhìn/roadmap ban đầu, phần lớn giá trị lịch sử; phần AI Coach/Relationship Map hiện tại (phần phức tạp nhất hệ thống) **hoàn toàn không được nhắc tới** vì được xây dựng sau.
- `documentation/MVP_LOCAL_PLAN.md` — checklist thực thi MVP tại thời điểm 2026-07-05, khi đó Energy/Orbit/AI Coach còn chưa có backend — nay phần lớn đã hoàn thành, chỉ còn giá trị tham khảo tiến trình.
- `database_schema.sql` (gốc repo) — schema mẫu cũ (chỉ 4 bảng), không phản ánh 12 bảng hiện tại.

Nguồn sự thật: mã nguồn Java trong `src/main/java/org/mentorship/reflectly/` (đặc biệt `controller/`, `service/`, `model/`, `ai/`) + `db/migration/V1__init_schema.sql` + `documentation/migrations/*.sql`.

---

## 10. Known limitations (giới hạn đã biết)

- Không có test tự động cho controller/repository (chỉ có 2 file unit test cho service); CI hiện tại chạy `mvn clean verify -DskipTests` nên **không có test nào thực sự chạy** trong pipeline.
- Chưa dùng công cụ quản lý migration chuẩn (Flyway) — thay đổi schema hiện quản lý thủ công.
- Ảnh đại diện lưu trên ổ đĩa cục bộ server — không bền vững khi redeploy.
- Mã hoá nội dung chat phụ thuộc biến môi trường `ENCRYPTION_KEY` được cấu hình đúng — nếu thiếu, dữ liệu lưu ở dạng chưa mã hoá.

---

## 11. Update Log

| Ngày (UTC) | Tác giả | Nội dung cập nhật |
|---|---|---|
| 2026-09-06 | Claude (agent) | Khởi tạo `spec.md` — khảo sát toàn bộ codebase backend hiện tại (API, schema 12 bảng, business logic AI Coach, auth/bảo mật, tích hợp bên thứ ba, tài liệu cũ) và viết tài liệu nghiệp vụ đầy đủ lần đầu tiên. |
| 2026-09-10 | Claude (agent) | Sửa lỗi `PrivateNetworkAccessFilter`: trước đây filter trả 200 và ngắt request ngay khi thấy header preflight Private Network Access (PNA) của trình duyệt mobile, khiến request không bao giờ chạm tới `CorsFilter` của Spring Security → thiếu các header CORS bắt buộc (`Access-Control-Allow-Origin`/`Allow-Methods`/`Allow-Headers`) trong response preflight đó → trình duyệt mobile chặn toàn bộ request và trả về "Network Error", khiến đăng nhập/đăng ký bằng username-password thất bại trên một số trình duyệt mobile. Giờ filter chỉ gắn thêm header `Access-Control-Allow-Private-Network` rồi để chain tiếp tục xử lý CORS bình thường. |
