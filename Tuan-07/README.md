# Tuần 07

Bài tập cá nhân tuần 07 môn Lập trình Thiết bị Di động.

---

## Danh sách bài tập

| Bài | Nội dung | Kiến thức chính |
|-----|----------|-----------------|
| [Thực hành 01](#thông-báo-theo-mvvm) | Danh sách thông báo và trạng thái trống | ViewModel, StateFlow, coroutine |
| [Bài tập về nhà](#thêm-công-việc-theo-mvvm) | Màn Việc mới và API tạo công việc | ViewModel, SavedStateHandle, Retrofit |
| [Bài Skills](./skills/) | Đơn xin nghỉ học, email giảng viên, slide thuyết trình | SKILL.md và mẫu tái sử dụng |

Hai bài MVVM dùng chung [mã nguồn UTH SmartTasks](../Tuan-06/UTHSmartTasks/). Mở project này trong Android Studio để chạy.

## Thông báo theo MVVM

1. Vào **Cài đặt → Thông báo → Demo thông báo MVVM**.
2. Màn Thông báo hiển thị bốn mục mẫu. Sau 10 giây, màn hình tự chuyển sang **Chưa có thông báo nào**.
3. Chọn **Chạy lại demo** để thử lại, **Thêm việc cần làm** để mở form, hoặc **Cài đặt nhắc việc** để quay về Cài đặt.

Bộ đếm tác động đến danh sách mẫu của bài thực hành. Tab **Thông báo** vẫn hiển thị thông báo từ task đang dùng.

## Thêm công việc theo MVVM

1. Thêm khóa `mockApiToken` vào `local.properties` của UTH SmartTasks, rồi build lại và chọn **Dùng thử không cần tài khoản** để sử dụng TryApps.
2. Bấm **+**. Nhập tên, chọn nhóm, ngày, giờ, nhắc việc và mức ưu tiên. **Tuần này** mở lịch từ thứ Hai đến Chủ nhật. Biểu tượng lịch mở bộ chọn ngày đầy đủ.
3. Chọn **Lưu** hoặc **Lưu công việc** để gửi `POST /tasks`. Khi API báo lỗi, form giữ dữ liệu đã nhập để thử lại. Khi lưu thành công, app mở Chi tiết.

Chế độ khách dùng dữ liệu mẫu cục bộ khi chưa đặt token. Để dùng Google Sign-In và Firestore, xem [README UTH SmartTasks](../Tuan-06/UTHSmartTasks/README.md).
