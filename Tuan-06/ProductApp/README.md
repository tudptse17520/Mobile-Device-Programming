# BÀI TẬP TUẦN 06 - XỬ LÝ MẠNG VÀ GỌI API (RETROFIT + JETPACK COMPOSE)

## 1. Giới thiệu bài tập
- **Chủ đề**: Gọi API RESTful lấy dữ liệu sản phẩm và hiển thị giao diện chi tiết sản phẩm (**Product detail**).
- **Endpoint API**: `GET https://mock.apidog.com/m1/890655-872447-default/v2/product`
- **Công nghệ áp dụng**:
  - **Kotlin** & **Jetpack Compose** (Material 3)
  - **Retrofit 2** & **Gson Converter**: Gọi API HTTP GET và parse JSON sang Kotlin Data Object.
  - **StateFlow** & **ViewModel**: Quản lý UI State (`Loading`, `Success`, `Error`) theo kiến trúc MVVM.
  - **Coil Compose**: Tải và hiển thị ảnh từ URL bất đồng bộ kèm caching.

---

## 2. Cấu trúc thư mục mã nguồn
```
app/src/main/java/com/example/productapp/
├── MainActivity.kt                      # Activity khởi chạy Compose
├── model/
│   └── Product.kt                       # Data class model ánh xạ JSON từ API
├── network/
│   ├── ApiService.kt                    # Interface định nghĩa endpoint GET
│   └── RetrofitClient.kt               # Singleton Retrofit instance
├── viewmodel/
│   └── ProductViewModel.kt              # ViewModel gọi API và cập nhật StateFlow
└── ui/
    ├── screen/
    │   └── ProductDetailScreen.kt       # Giao diện Product Detail đúng chuẩn design mẫu
    └── theme/
        └── Theme.kt                     # Cấu hình màu sắc, typography Material 3
```

---

## 3. Các tính năng đã hoàn thiện
1. **Gọi API bất đồng bộ (Coroutine + suspend fun)**: Đảm bảo không block Main Thread.
2. **Quản lý 3 trạng thái giao diện**:
   - `Loading`: Hiển thị vòng xoay tròn tải dữ liệu (`CircularProgressIndicator`).
   - `Success`: Hiển thị chi tiết sản phẩm: Ảnh sản phẩm, tên, giá tiền định dạng VNĐ, mô tả.
   - `Error`: Hiển thị lỗi mạng kèm nút **"Thử lại"** (`Retry`).
3. **Giao diện chuẩn theo mockup đề bài**:
   - TopAppBar với tiêu đề **Product detail** màu xanh và nút back.
   - Khung hình ảnh sản phẩm bo góc 16dp với nền màu cam nổi bật.
   - Tên sản phẩm in đậm, giá tiền định dạng màu đỏ nổi bật (`4.000.000đ`).
   - Khung card màu xám nhạt bo góc chứa mô tả chi tiết sản phẩm.
   - Hỗ trợ cuộn trang mượt mà (`verticalScroll`).
