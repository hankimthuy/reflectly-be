# MimoSe — Hệ thống tính năng & Lộ trình phát hành

> Tài liệu này tổng hợp toàn bộ mặt bằng tính năng mà MimoSe có thể cung cấp, sắp xếp theo lớp kiến trúc sản phẩm (dựa trên 3 trụ cột đã định nghĩa trong PRD: Innerverse – Outerverse – Bridge), và đề xuất lộ trình release theo phase để có bản MVP dùng được sớm, sau đó bổ sung dần.

## 1. Nguyên tắc chọn & sắp xếp tính năng

- **Đồng hành, không lý thuyết**: mỗi tính năng phải gắn với một hành động cụ thể user có thể làm ngay, không phải nội dung để đọc.
- **Tự giải quyết**: app đưa công cụ/khung hỏi, không đưa lời khuyên thay user.
- **Protocol có vòng đời**: cách giải quyết không phải "làm 1 lần xong", cần theo dõi hiệu quả theo thời gian và báo khi cần điều chỉnh.
- **Tiện lợi tại đúng khoảnh khắc**: ưu tiên thao tác nhanh (1-2 chạm) hơn là form dài, đặc biệt với các tính năng dùng lúc đang stress.

## 2. Kiến trúc tính năng theo lớp

### Layer 0 — Nền tảng (đã có)
Auth (Google OAuth + username/password), User profile, Journal Entries (CRUD tự do).

### Layer 1 — Innerverse (Nội tâm)
| Tính năng | Mô tả |
|---|---|
| Energy tracking nhanh | Log năng lượng 1-chạm (thang điểm), gắn context tag (công việc, xã hội, nghỉ ngơi...) |
| Core values | User tự định nghĩa 3-5 giá trị cốt lõi, dùng làm bộ lọc khi ra quyết định |
| Emotion/mood tagging | Gắn cảm xúc cụ thể vào entry/energy log thay vì chỉ số chung chung |
| Biểu đồ xu hướng | Trực quan hóa năng lượng theo thời gian và theo context tag |

### Layer 2 — Outerverse (Ngoại giới)
| Tính năng | Mô tả |
|---|---|
| Danh bạ mối quan hệ | Thêm người quan trọng trong đời sống user |
| Orbit system | Phân loại độ gần (1-5) theo tần suất tương tác + xếp hạng chủ quan |
| Impact assessment | Đo mối quan hệ này ảnh hưởng năng lượng user thế nào theo thời gian |
| Nhật ký tương tác | Ghi lại các tương tác đáng chú ý với từng người |

### Layer 3 — Bridge (Cầu nối hành động) — lõi USP
| Tính năng | Mô tả |
|---|---|
| Action Protocol | Kịch bản ứng phó do user tự viết hoặc chọn từ mẫu, gắn theo tình huống/trigger cụ thể |
| Script templates | Mẫu câu, kịch bản giao tiếp cho tình huống khó (seed ban đầu từ chính trải nghiệm của bạn) |
| Protocol effectiveness tracking | Mỗi lần áp dụng protocol, ghi nhận kết quả; hệ thống phát hiện xu hướng giảm hiệu quả theo thời gian |
| Pattern recognition | Liên kết dữ liệu Inner (năng lượng) – Outer (orbit) – Bridge (protocol) để gợi ý đúng lúc |

### Layer 4 — Companion (Đồng hành chủ động)
| Tính năng | Mô tả |
|---|---|
| Smart nudge | Thông báo đúng lúc dựa trên pattern phát hiện được (không phải nhắc chung chung) |
| Memory resurfacing | Nhắc lại entry/protocol cũ liên quan khi user gặp tình huống tương tự |
| "Đọc lại hiện tại" | Tổng hợp khách quan pattern gần đây, trình bày lại cho user tự nhận ra thay vì app kết luận thay |
| Check-in định kỳ | Tự-audit định kỳ (theo tuần/tháng/quý) |

### Layer 5 — Insight & Growth
Dashboard thống kê tổng hợp, quotes/nội dung cá nhân hóa theo pattern, growth timeline/milestones.

### Layer 6 — Mở rộng dài hạn (tùy chọn, cân nhắc kỹ)
AI-assisted reflection/summarization, chia sẻ protocol ẩn danh giữa cộng đồng có hoàn cảnh tương tự, tích hợp chuyên gia tham vấn, tích hợp wearable/calendar.

## 3. Lộ trình theo Phase

### Phase 1 — MVP dùng được ngay
**Mục tiêu:** tạo vòng lặp giá trị tối thiểu — ghi nhận → phản chiếu → protocol cá nhân — không cần Outerverse hay AI.

- Giữ nguyên Layer 0 đã có (Auth, Profile, Entries).
- Energy tracking cơ bản (1-chạm + context tag) — nền cho mọi phân tích sau này.
- Entry templates theo 2-3 chủ đề bạn có trải nghiệm sâu nhất (thay trang trắng bằng câu hỏi dẫn dắt).
- Action Protocol cơ bản: tạo/sửa/đánh dấu "đã dùng" + ghi kết quả ngắn (hiệu quả/không).
- Dashboard đơn giản: biểu đồ năng lượng theo thời gian.

*Vì sao dừng ở đây:* đây là bộ tối thiểu để user thấy giá trị thật (không chỉ viết mà còn có nơi lưu & tái sử dụng cách giải quyết), đồng thời bắt đầu tích lũy dữ liệu cần cho Phase 2.

### Phase 2 — Đồng hành chủ động
Cần dữ liệu tích lũy từ Phase 1 để cá nhân hóa được.

- Outerverse: danh bạ, orbit system, impact assessment.
- Protocol effectiveness tracking đầy đủ (log mỗi lần dùng, phát hiện xu hướng giảm hiệu quả).
- Smart nudge dựa trên pattern thực tế đã có.
- Memory resurfacing (gợi lại entry/protocol cũ liên quan).

### Phase 3 — Insight sâu
- "Đọc lại hiện tại" — gương phản chiếu định kỳ, tổng hợp Inner + Outer + Bridge.
- Pattern recognition nâng cao liên kết 3 lớp.
- Cá nhân hóa nội dung/quotes theo pattern thực tế của từng user.

### Phase 4 — Tăng trưởng & cộng đồng (dài hạn, cân nhắc theo nhu cầu thị trường)
- Chia sẻ protocol ẩn danh giữa các user có hoàn cảnh tương tự.
- Tích hợp chuyên gia/coach khi pattern cho thấy cần hỗ trợ chuyên sâu hơn app tự làm được.
- AI-assisted summarization, tích hợp wearable/calendar.

## 4. Điều kiện chuyển phase (gợi ý)

- **Phase 1 → 2**: user tạo được trung bình ≥2-3 protocol/tháng và quay lại dùng entry template đều đặn — cho thấy vòng lặp Layer 1+3 có giá trị thật, đủ nền để mở rộng sang Outerverse.
- **Phase 2 → 3**: có đủ dữ liệu lịch sử (ví dụ ≥3 tháng liên tục) để pattern recognition cho ra gợi ý có ý nghĩa, không phải suy diễn từ dữ liệu quá ít.
- **Phase 3 → 4**: đã có tệp user gắn bó ổn định, nhu cầu vượt ngoài khả năng tự-giải-quyết bắt đầu xuất hiện rõ (feedback yêu cầu kết nối chuyên gia/cộng đồng).

## 5. Rủi ro & giả định cần validate trước khi build sâu

- Giả định "user sẽ log năng lượng đều đặn" cần test bằng bản thử nghiệm nhỏ trước khi đầu tư nhiều vào phân tích pattern.
- Giả định "protocol tự viết sẽ hữu ích hơn nội dung có sẵn" nên được kiểm chứng qua phỏng vấn 5-10 user mục tiêu trước Phase 1.
- Tính năng "đọc lại hiện tại" cần cẩn trọng về ranh giới không đưa lời khuyên tâm lý — nên tham khảo ý kiến chuyên môn tâm lý khi thiết kế ngôn ngữ hiển thị.
