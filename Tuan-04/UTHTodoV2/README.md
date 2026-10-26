# Bài tập 1: UTHTodoV2 (Navigation + OOP)

## 1. Giới thiệu
Ứng dụng **UTH Todo V2** là phiên bản thực hành xây dựng giao diện với Jetpack Compose và điều hướng Navigation Compose, quản lý danh sách công việc thông qua mô hình lập trình hướng đối tượng (OOP).

## 2. Cấu trúc màn hình và Luồng điều hướng (Navigation)
Luồng điều hướng được cài đặt bằng Navigation Compose trong `MainActivity.kt`:
```text
splash ──(tự động hoặc click)──> onboarding/1 ──(Tiếp tục)──> onboarding/2 ──(Tiếp tục)──> onboarding/3 ──(Bắt đầu ngay)──> todo
   │                                  │                              │                              │
   └────────────(Bỏ qua)──────────────┴──────────────(Bỏ qua)────────┴──────────────(Bỏ qua)────────┘
```

- **Màn hình 1 — Splash Screen (`splash`)**: Nền xanh `#2F5BEA`, biểu tượng UTH Todo, tự động điều hướng sang Onboarding sau 1.8 giây hoặc khi người dùng chạm vào màn hình.
- **Màn hình 2 — Onboarding bước 1 (`onboarding/1`)**: "Mọi việc, gọn trong một chỗ" với minh họa danh sách công việc.
- **Màn hình 3 — Onboarding bước 2 (`onboarding/2`)**: "Chia theo Học tập và Cuộc sống" với minh họa nhãn danh mục.
- **Màn hình 4 — Onboarding bước 3 (`onboarding/3`)**: "Nhắc đúng lúc" với minh họa chuông thông báo.
- **Màn hình 5 — Todo (`todo`)**: Hiển thị tiến độ hoàn thành, bộ lọc danh mục (Tất cả, Học tập, Cuộc sống), danh sách công việc và chức năng thêm công việc mới.

## 3. Thể hiện Lập trình Hướng đối tượng (OOP)
1. **Lớp dữ liệu và Đóng gói (Encapsulation):**
   - Lớp `TodoTask` quản lý thông tin công việc (`id`, `title`, `category`, `time`) và trạng thái hoàn thành `isCompleted` có `private set`.
   - Trạng thái chỉ được thay đổi thông qua phương thức nghiệp vụ `toggleCompleted()`, `markCompleted()`, `markIncomplete()`.
2. **Enum Class:**
   - `TaskCategory` gồm `STUDY` ("Học tập") và `LIFE` ("Cuộc sống") lưu kèm mã màu và nhãn hiển thị.
3. **Data Class:**
   - `OnboardingPage` đóng gói thông tin từng trang onboarding (`step`, `title`, `description`, `buttonText`).

## 4. Cách build và chạy
Mở project trong Android Studio hoặc dùng lệnh:
```bash
./gradlew assembleDebug
```
