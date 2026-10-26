# Bài tập 2: LibraryManagementApp (Hệ thống Quản lý Thư viện)

## 1. Giới thiệu
Ứng dụng **Hệ thống Quản lý Thư viện** được xây dựng bằng Kotlin và Jetpack Compose theo đúng thiết kế tham chiếu. Ứng dụng quản lý việc mượn/trả sách giữa sinh viên và thư viện, thể hiện đầy đủ **4 đặc trưng cốt lõi của Lập trình Hướng Đối Tượng (OOP)**.

## 2. Các chức năng đã triển khai (Khớp thiết kế)
1. **Màn hình Quản lý chính (Khớp 4 ảnh tham chiếu):**
   - Chọn và hiển thị thông tin sinh viên hiện tại (`Nguyen Van A`, `Nguyen Thi B`, `Nguyen Van C`) qua nút **Thay đổi**.
   - Hiển thị danh sách sách mà sinh viên đó đang mượn bên trong khung màu xám.
   - Khi sinh viên chưa mượn sách (như Nguyen Van C), hiển thị thông điệp:
     > *"Bạn chưa mượn quyển sách nào. Nhấn 'Thêm' để bắt đầu hành trình đọc sách!"*
   - Nút **Thêm** cho phép sinh viên mượn thêm sách còn khả dụng.
   - Nhấn vào sách cho phép xem chi tiết và thực hiện trả sách.
2. **Tab Danh sách Sách (DS Sách):**
   - Hiển thị toàn bộ kho sách của thư viện, trạng thái Khả dụng hay Đang mượn.
   - Hiển thị đa hình loại sách, thời hạn mượn và phí phạt trễ hạn.
3. **Tab Sinh viên:**
   - Danh sách sinh viên và số lượng sách mỗi người đang mượn.

## 3. Thể hiện đầy đủ 4 đặc trưng OOP trong mã nguồn

### A. Đóng gói (Encapsulation)
- Trong lớp `Book`: Các trường `_borrowedByStudentId` và `_borrowedByStudentName` được đặt `private`.
- Không composable nào có thể tự ý gán thông tin người mượn hoặc sửa trạng thái trực tiếp.
- Mọi thao tác mượn/trả bắt buộc phải thông qua hai phương thức nghiệp vụ:
  - `borrow(student: Student): Boolean`: Kiểm tra xem sách có đang khả dụng không (`isAvailable()`) trước khi gán.
  - `returnItem(): Boolean`: Kiểm tra ràng buộc sách đang mượn trước khi hoàn trả.

### B. Trừu tượng (Abstraction)
- Định nghĩa interface `Borrowable` quy định hợp đồng cho các tài liệu có thể mượn:
  - `borrow(student: Student): Boolean`
  - `returnItem(): Boolean`
  - `isAvailable(): Boolean`
  - `getLoanDurationDays(): Int`
  - `calculateLateFee(daysLate: Int): Long`
- Lớp trừu tượng `Book` hiện thực `Borrowable` và khai báo các phương thức trừu tượng `getBookTypeLabel()`, `getSpecialAttribute()`.
- Lớp trừu tượng `Person` định nghĩa thuộc tính và phương thức chung `getRole()`.

### C. Kế thừa (Inheritance)
- `Student` kế thừa từ lớp cha `Person(id, name)`.
- `TextBook` (Giáo trình) kế thừa từ lớp cha `Book(id, title, author)`, bổ sung thêm môn học (`subject`) và học kỳ (`semester`).
- `ReferenceBook` (Sách tham khảo) kế thừa từ `Book`, bổ sung lĩnh vực (`field`) và nhà xuất bản (`publisher`).

### D. Đa hình (Polymorphism)
- Các đối tượng `TextBook` và `ReferenceBook` được lưu trữ và xử lý đồng nhất trong danh sách `List<Book>`.
- Khi gọi các hàm được override, chương trình hành xử khác nhau tùy thuộc vào kiểu đối tượng thực tế:
  - `getLoanDurationDays()`: Trả về **14 ngày** đối với Giáo trình, nhưng chỉ **7 ngày** đối với Sách tham khảo.
  - `calculateLateFee(daysLate)`: Tính phí trễ **2.000 VNĐ/ngày** đối với Giáo trình, nhưng **5.000 VNĐ/ngày** đối với Sách tham khảo.
  - `getBookTypeLabel()`: Trả về nhãn hiển thị tương ứng "Giáo trình" hoặc "Sách tham khảo".

## 4. Cách build và chạy
Tại thư mục `LibraryManagementApp`:
```bash
./gradlew assembleDebug
```
