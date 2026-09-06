# Hướng dẫn cho agent làm việc trên repo này

## Duy trì `spec.md` (bắt buộc)

`spec.md` ở repo này là **tài liệu nghiệp vụ sống** (living business/BA documentation) mô tả toàn bộ hệ thống backend: kiến trúc, database, API, business logic (đặc biệt nhóm AI Coach), auth/bảo mật, tích hợp bên thứ ba. Nó **phải luôn phản ánh đúng code hiện tại**, không phải kế hoạch/kỳ vọng.

Có một tài liệu song song ở frontend: `reflectly-fe/spec.md` (màn hình, luồng UI, tính năng theo trải nghiệm người dùng).

### Sau khi hoàn thành bất kỳ thay đổi nào ảnh hưởng đến:
- schema database (thêm/sửa/xoá bảng, cột, quan hệ);
- endpoint API (thêm mới, đổi path/method, đổi request/response, xoá endpoint);
- business logic quan trọng (đặc biệt luồng AI Coach: chat, tóm tắt, trích xuất bộ nhớ);
- cơ chế auth/bảo mật (luồng đăng nhập, quyền hạn, rate-limit, mã hoá);
- tích hợp bên thứ ba mới (thêm/đổi/gỡ một dịch vụ ngoài);

→ agent **phải**:

1. Mở `spec.md`, tìm đúng phần bị ảnh hưởng, và cập nhật cho khớp hiện trạng mới. Không chỉ thêm — nếu một API/bảng đã bị xoá hoặc đổi hành vi, phải sửa/xoá nội dung cũ tương ứng, không để tài liệu nói sai.
2. Nếu một domain đang được frontend dùng trở nên không còn được dùng (hoặc ngược lại, một domain "chết" ở mục 8 được hồi sinh), cập nhật đúng mục tương ứng.
3. Thêm một dòng mới vào bảng **Update Log** ở cuối `spec.md`: ngày (UTC, định dạng `YYYY-MM-DD`), tác giả (ví dụ "Claude (agent)"), mô tả ngắn gọn 1 câu về thay đổi và (nếu có) số PR/commit liên quan.
4. Nếu thay đổi ảnh hưởng tới frontend (đổi endpoint, đổi field response, thêm domain mới cần UI), cân nhắc ghi chú rằng `reflectly-fe/spec.md` cũng cần cập nhật tương ứng (không bắt buộc tự sửa repo kia, nhưng nên nhắc trong PR description nếu áp dụng).

### Không bắt buộc cập nhật khi:
- refactor thuần kỹ thuật không đổi hành vi/API quan sát được từ bên ngoài;
- sửa lỗi nhỏ không ảnh hưởng luồng nghiệp vụ mô tả trong spec.md;
- thay đổi cấu hình hạ tầng/CI không ảnh hưởng nghiệp vụ.

### Nguyên tắc viết
- Giữ văn phong hiện có của `spec.md` (tiếng Việt, mô tả nghiệp vụ, tránh lặp lại chi tiết implementation nếu không cần thiết để hiểu nghiệp vụ).
- Chỉ cập nhật phần bị ảnh hưởng — không viết lại toàn bộ tài liệu trong một lần sửa.
- Nếu phát hiện một phần của `spec.md` đã sai/lỗi thời trong lúc làm việc khác (dù không liên quan trực tiếp tới thay đổi của mình), nên sửa luôn hoặc ít nhất ghi chú lại trong Update Log.
