# Bài tập 3: DataFlowNavigationApp (Nav Data Flow)

## 1. Giới thiệu
Ứng dụng **DataFlowNavigationApp** (SmartTasks) mô phỏng quy trình đặt lại mật khẩu và xác thực tài khoản 4 bước, minh họa trực quan và chính xác cơ chế **Data Flow** (truyền nhận dữ liệu 2 chiều) qua Navigation Compose theo đúng sơ đồ ảnh thiết kế của giảng viên.

## 2. Sơ đồ Luồng Dữ liệu (Data Flow Architecture)
```text
[Màn 1: Forget Password]
   │  (Nhập Email)
   ▼  navigate("verify_code/{email}")
[Màn 2: Verify Code]
   │  (Hiển thị Email nhận được + Nhập OTP)
   ▼  navigate("reset_password/{email}/{otp}")
[Màn 3: Reset Password]
   │  (Nhập & Xác thực Password)
   ▼  navigate("confirm/{email}/{otp}/{password}")
[Màn 4: Confirm]
   │  (Hiển thị tổng hợp: 👤 Email, ✉ OTP, 🔒 Password)
   │
   └──(Bấm "Summit")─── Mũi tên đỏ quay về ───► [Màn 1: Forget Password]
                                                  Hiển thị Card kết quả:
                                                  ┌───────────────────────────┐
                                                  │ email: uth@gmail.com      │
                                                  │ otp: 12345                │
                                                  │ pass: 12345678            │
                                                  │ (từ màn hình 4)           │
                                                  └───────────────────────────┘
```

## 3. Cách truyền và nhận dữ liệu trong code
1. **Truyền từ Màn hình 1 sang Màn hình 2:**
   - Code: `navController.navigate("verify_code/${Uri.encode(email)}")`
   - Nhận: `val email = Uri.decode(backStackEntry.arguments?.getString("email"))`
2. **Truyền từ Màn hình 2 sang Màn hình 3:**
   - Dữ liệu tích lũy: `email` và `otp`
   - Code: `navController.navigate("reset_password/$encodedEmail/$encodedOtp")`
3. **Truyền từ Màn hình 3 sang Màn hình 4:**
   - Dữ liệu tích lũy: `email`, `otp`, và `password`
   - Code: `navController.navigate("confirm/$encodedEmail/$encodedOtp/$encodedPassword")`
4. **Truyền ngược từ Màn hình 4 về Màn hình 1 (Hoàn tất chu trình):**
   - Đóng gói đối tượng `PasswordResetFlowData(email, otp, password)`
   - Cập nhật state kết quả và chuyển về màn 1:
     `navController.navigate("forget_password") { popUpTo("forget_password") { inclusive = true } }`
   - Màn hình 1 lập tức render Card màu xanh dương nhạt với chữ đỏ hiển thị toàn bộ 3 trường dữ liệu khớp với thiết kế có mũi tên đỏ của giảng viên.

## 4. Cách build và chạy
Tại thư mục `DataFlowNavigationApp`:
```bash
./gradlew assembleDebug
```
