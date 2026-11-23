---
name: lam-slide-thuyet-trinh
description: >-
  Tạo slide thuyết trình cho sinh viên đại học.
  Dùng khi sinh viên cần làm bài thuyết trình, trình bày đề tài,
  báo cáo môn học, seminar, hoặc bất kỳ bài trình bày nào cần slide.
  Cũng dùng khi sinh viên nói "làm PowerPoint", "tạo bài trình bày",
  "chuẩn bị thuyết trình", "làm presentation", "viết slide",
  "báo cáo đồ án", "trình bày đề tài".
  Không dùng cho việc viết báo cáo dạng văn bản (Word/PDF) hay
  các tài liệu không phải slide trình chiếu.
---

# Làm slide thuyết trình

Skill này giúp sinh viên tạo bộ slide thuyết trình chuyên nghiệp, có cấu trúc
rõ ràng cho bất kỳ môn học hay đề tài nào.

## Quy trình

### Bước 1: Thu thập thông tin

Hỏi sinh viên:
- **Đề tài** thuyết trình là gì?
- **Môn học** nào?
- **Thời lượng** trình bày (bao nhiêu phút)?
- **Yêu cầu đặc biệt** từ giảng viên (nếu có): số slide tối thiểu/tối đa,
  nội dung bắt buộc, format cụ thể...
- **Đối tượng** nghe: giảng viên, sinh viên cùng lớp, hội đồng...

### Bước 2: Lập dàn ý (Outline)

Dựa trên thông tin thu thập, tạo dàn ý theo cấu trúc chuẩn trong
[assets/slide-template.md](./assets/slide-template.md). Trình bày dàn ý cho
sinh viên duyệt trước khi viết chi tiết.

**Quy tắc ước lượng số slide**:
- Thuyết trình 5 phút → 5–7 slide
- Thuyết trình 10 phút → 8–12 slide
- Thuyết trình 15 phút → 12–18 slide
- Thuyết trình 20 phút → 15–22 slide
- Thuyết trình 30 phút → 20–30 slide

### Bước 3: Viết từng slide

Viết nội dung chi tiết cho từng slide, tuân thủ các quy tắc trình bày:

#### Quy tắc trình bày slide

1. **Tiêu đề slide**: Ngắn gọn, rõ ràng, tóm tắt nội dung chính.
2. **Nội dung**: Dùng bullet points, mỗi ý tối đa 1–2 dòng.
3. **Quy tắc 6×6**: Tối đa 6 bullet points, mỗi bullet tối đa 6 từ
   (linh hoạt nhưng tránh nhồi chữ).
4. **Hình ảnh**: Gợi ý chỗ nên chèn hình minh họa, biểu đồ, sơ đồ.
5. **Ghi chú thuyết trình (Speaker Notes)**: Viết ghi chú ngắn cho người
   trình bày ở mỗi slide, gợi ý nên nói gì.
6. **Không copy nguyên văn**: Slide là điểm nhấn, không phải bài viết.

### Bước 4: Review và hoàn thiện

- Đọc lại toàn bộ slide cho sinh viên duyệt.
- Kiểm tra tính logic, mạch lạc giữa các slide.
- Đề xuất cải thiện nếu cần.

## Định dạng output

Mỗi slide được viết theo format Markdown:

```markdown
---

## Slide 1: {{Tiêu đề}}

- Ý chính 1
- Ý chính 2
- Ý chính 3

> 🎤 **Ghi chú**: {{Gợi ý cho người trình bày}}

---
```

## Lưu ý

- Không tự bịa nội dung chuyên ngành — hỏi sinh viên hoặc dựa trên tài liệu
  sinh viên cung cấp.
- Slide đầu tiên luôn là **Trang bìa** (tiêu đề, tên nhóm/cá nhân, môn, GV).
- Slide cuối cùng luôn là **Cảm ơn & Q/A**.
- Nếu có tài liệu tham khảo, thêm slide **Tài liệu tham khảo** trước slide
  cuối.
