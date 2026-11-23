# UTH SmartTasks — UTH Todo V2

Ứng dụng Todo bằng Kotlin, Jetpack Compose, ViewModel, Retrofit, Firebase Authentication và Cloud Firestore. Các flow gồm onboarding, trang chủ, lọc/tìm kiếm danh sách, tạo/sửa/xóa công việc, đánh dấu hoàn thành, việc nhỏ trong chi tiết, nhắc giờ, thông báo, hồ sơ Google và cài đặt giao diện.

## Mock API cho chế độ khách

Base URL: `https://share.tryapps.store/mock/teach/`

| Method | Đường dẫn | Dùng cho |
|---|---|---|
| GET | `tasks` | Lấy danh sách |
| GET | `tasks/{id}` | Lấy chi tiết |
| POST | `tasks` | Tạo công việc |
| PATCH | `tasks/{id}` | Sửa trường công việc, trạng thái và việc nhỏ |
| DELETE | `tasks/{id}` | Xóa công việc |

Đặt Bearer token do lớp cấp trong `local.properties` tại thư mục project (file này không commit):

```properties
mockApiToken=YOUR_MOCK_API_TOKEN
```

Khi chưa có token, app dùng danh sách mẫu cục bộ. Dữ liệu mẫu chỉ giữ đến khi app bị đóng. Mock API có thể trả mẫu cố định thay vì lưu CRUD; giao diện vẫn báo lỗi đồng bộ khi server từ chối request.

## Google Sign-In và đồng bộ task

Firebase Auth dùng Credential Manager. Đặt cấu hình Android của project `uth-smart-tasks-821b9`, package `com.example.uthsmarttasks`, vào `app/google-services.json`. File này được Git bỏ qua. Cấu hình Google Sign-In phải có fingerprint SHA phù hợp với khóa ký APK.

Tạo Cloud Firestore với database ID `(default)` và áp dụng `firestore.rules` bằng Firebase Console hoặc Firebase CLI đã đăng nhập:

```powershell
npx firebase-tools deploy --only firestore:rules --project uth-smart-tasks-821b9
```

Khi đăng nhập Google, ứng dụng đọc và ghi task trong `users/{uid}/tasks/{taskId}`. Rules chỉ cho tài khoản sở hữu UID đó truy cập. Firestore cập nhật danh sách khi có thay đổi từ thiết bị khác và gửi các thay đổi đang chờ khi có mạng. Kho task mới bắt đầu rỗng. Task thử của chế độ khách tách riêng khỏi tài khoản Google.

## Thông báo trên điện thoại

Trong **Cài đặt → Thông báo**, bật **Nhắc việc** và cho phép thông báo của ứng dụng trong cài đặt Android. Mỗi task cần bật **Nhắc tôi** và có ngày giờ đến hạn trong tương lai. Lịch đặt trước hạn 15 phút, sớm nhất là đầu ngày đến hạn theo giờ máy. Sửa hạn, hoàn thành, xóa hoặc tắt nhắc việc sẽ cập nhật lịch trên thiết bị.

Trên Android 12 trở lên, mục **Nhắc đúng giờ** mở quyền **Báo thức và lời nhắc**. Khi quyền chưa được cấp, Android có thể trì hoãn thông báo. Tuỳ chọn **Tổng kết cuối ngày** đặt thông báo lúc 21:00 theo giờ máy. Thiết bị khôi phục lịch đã lưu khi khởi động lại và làm mới từ Firestore khi ứng dụng nhận dữ liệu đồng bộ.

## Crashlytics, FCM và ảnh đại diện

- **Crashlytics** gửi báo cáo crash về project Firebase hiện có. Các lỗi Firestore được ghi có chọn lọc theo thao tác và mã lỗi, với stack trace đã bỏ thông báo gốc. Xem báo cáo trong Firebase Console → Crashlytics.
- **FCM** nhận thông báo trên kênh **Thông báo từ máy chủ**. Trong APK debug, vào **Cài đặt → Thông báo Android → Mã FCM kiểm thử** để sao chép mã thiết bị. Trong Firebase Console → Messaging, chọn **Send test message** và dán mã đó để gửi đến thiết bị này. Điện thoại cần quyền thông báo. Lịch nhắc trước hạn 15 phút vẫn chạy trực tiếp trên Android.
- **Ảnh đại diện** dùng Storage Emulator trên máy phát triển. Chạy lệnh bên dưới từ thư mục này, mở APK debug trên Android Emulator, đăng nhập Google rồi bấm nút camera ở **Hồ sơ** để chọn một ảnh. App lưu JPEG đã thu nhỏ vào `users/{uid}/avatar.jpg` và đọc lại khi mở Hồ sơ. Rules giới hạn quyền theo UID. Ảnh demo có thể mất khi Storage Emulator dừng nếu không xuất dữ liệu.

```powershell
npx -y firebase-tools@latest emulators:start --only storage --project uth-smart-tasks-821b9
```

## Mở và chạy

Mở thư mục này trong Android Studio, chọn cấu hình `app` rồi Run. Build APK debug bằng:

```powershell
.\gradlew.bat assembleDebug
```
