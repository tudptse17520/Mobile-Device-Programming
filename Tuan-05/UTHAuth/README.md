# UTHAuth — Bài tập 02

Project độc lập trong thư mục Tuan-05/UTHAuth, tái sử dụng Google Sign-In bằng Credential Manager, font Be Vietnam Pro, theme và vector drawable của FirebaseAuth.

## Tính năng đã triển khai

- Google, email/mật khẩu, đăng ký, đặt lại mật khẩu, Firebase Anonymous Auth.
- Trang chủ theo Figma: lời chào theo tài khoản, tiến độ đúng ngày, số việc chưa xong theo danh mục, checkbox, thanh điều hướng và FAB.
- Thêm/sửa/xóa có xác nhận; ngày, giờ, ghi chú; tìm kiếm; lọc danh mục/trạng thái; chia sẻ nội dung bằng Android Sharesheet.
- Realtime Database tại users/{uid}/tasks/{taskId}. Listener cập nhật tiến độ; persistence trên đĩa và hàng đợi ghi khi mất mạng. Quyền từ chối sẽ rollback thay đổi local và báo lỗi.
- Analytics: login, sign_up, task_created, task_updated, task_completed, task_reopened, task_deleted, category_filtered; setUserId theo phiên đăng nhập.
- Remote Config: ba lời động viên theo tiến độ, có mặc định trong XML, nút tải cấu hình trong Cài đặt.
- Thêm dữ liệu mẫu bằng nút riêng trong Cài đặt: 4 việc cho ngày hiện tại, 2 việc hoàn thành. Không tự tạo lại khi danh sách trống. Nhấn lại cùng ngày không ghi đè các ID mẫu đang có.
- Preview HomeScreenPreview dùng dữ liệu mẫu, không cần đăng nhập.

## Chạy trên máy này

1. Android Studio > Open > chọn thư mục UTHAuth (mở cửa sổ mới).
2. Đặt cấu hình Android của project Firebase `fir-auth-uth` vào `app/google-services.json`, sau đó chạy Gradle Sync và mở app.
3. Giữ applicationId com.example.firebaseauth để tái sử dụng OAuth client/SHA hiện có. Trên cùng thiết bị, bản này cập nhật app FirebaseAuth cùng package; hai project chưa cài song song được. Muốn cài song song, đăng ký Android app mới trên cùng Firebase project, đổi applicationId và lấy google-services.json tương ứng.
4. Trong Firebase Console của fir-auth-uth, bật Google, Email/Password và Anonymous nếu muốn dùng cả ba. Google Sign-In cần SHA-1/SHA-256 của keystore ký ứng dụng.
5. Mở Realtime Database, tạo database nếu chưa có. Sao chép URL chính xác của instance (không phải link Console).
6. Dán nội dung database.rules.json vào tab Rules của đúng instance, kiểm tra và Publish. Rules chỉ cho chủ UID đọc/ghi tasks của mình, kiểm tra schema và từ chối trường lạ. Chưa deploy tự động lên cloud.
7. Đăng nhập app > Cài đặt > nhập URL > Lưu và kết nối. URL lưu cục bộ; không phải mật khẩu.
8. Nhấn “Thêm 4 công việc mẫu hôm nay”; về Trang chủ kiểm tra 2/4 và 50%. Tick một việc chưa xong để thấy 75%.
9. Remote Config: tạo/publish các key trong app/src/main/res/xml/remote_config_defaults.xml rồi nhấn nút tải. Fetch có khoảng cách tối thiểu 1 giờ.

CLI trên máy hiện tại chạy được kiểm tra version nhưng database:instances:list bị lỗi phụ thuộc Node (MODULE_NOT_FOUND), vì vậy chưa xác minh URL database, provider và rules đang chạy trên cloud. Không có service account/private key được thêm vào dự án.

Khi đã sửa Firebase CLI và đăng nhập, có thể lấy lại cấu hình Android bằng:
```powershell
npx -y firebase-tools@latest apps:list --project fir-auth-uth
npx -y firebase-tools@latest apps:sdkconfig ANDROID <FIREBASE_APP_ID> --project fir-auth-uth
```
Lưu phần JSON cấu hình trả về vào app/google-services.json đúng với package đã đăng ký.

## Kiểm thử

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Unit test kiểm tra tính tiến độ theo ngày, số việc còn lại, ngày/giờ sai; kế thừa test xử lý lỗi auth và initials.
Instrumented test kiểm tra auth UI, checkbox, form nhập và hành trình Trang chủ → hoàn thành → lọc danh mục.
Reports: app/build/reports/tests/testDebugUnitTest và app/build/reports/androidTests/connected/debug.
APK debug: app/build/outputs/apk/debug/app-debug.apk.
Đã kiểm thử Google Sign-In với Firebase production trên Pixel_10 và khôi phục phiên sau đóng/mở app. Chưa kiểm thử database production, đồng bộ hai thiết bị hay rules qua emulator.

## Phạm vi mở rộng

Hiện dùng Authentication + Realtime Database + Analytics + Remote Config. Chọn một nguồn dữ liệu chính, chưa đồng bộ trùng sang Firestore.
Tab Thông báo chỉ có tổng kết tiến độ; chưa có FCM, nhắc việc theo giờ hay notification hệ thống.
Chưa có upload avatar/tệp (Storage), Crashlytics, App Check, Hosting/App Links, In-App Messaging hoặc AdMob.
Nên mở rộng tiếp Crashlytics và FCM khi cần báo lỗi/nhắc việc; Storage khi có yêu cầu đính kèm. Quảng cáo không cần cho bài quản lý công việc cơ bản.
Tài khoản khách có UID Firebase thật nhưng chưa có chức năng liên kết tài khoản; nên dùng Google/email cho dữ liệu cần giữ lâu dài.

## Thiết kế và tài liệu

Link được cung cấp trỏ tới trang thực hành UI; màn Todo nằm ở page UTH Todo V2 trong cùng file.
Tham chiếu Home sáng: https://www.figma.com/design/ivYBomXm4kJqddjjzvqhl1/UTH-Mobile--Copy-?node-id=17870-746
Giữ bố cục, màu, font và icon, thay số liệu mẫu trong Figma bằng dữ liệu thật. Chưa tuyên bố pixel-perfect; lịch và nội dung dài sẽ ảnh hưởng chiều cao.
Ứng dụng dùng các vector drawable được tái sử dụng từ app gốc.

Tài liệu chính thức: [Realtime Database offline](https://firebase.google.com/docs/database/android/offline-capabilities), [Remote Config Android](https://firebase.google.com/docs/remote-config/android/get-started).
