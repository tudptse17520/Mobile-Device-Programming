# Crypto Market — Nâng cao 02

Giao diện thị trường crypto theo mẫu Figma: banner tin mới, danh sách coin yêu thích, biểu đồ mini và biến động 24 giờ. Kotlin + Jetpack Compose; Retrofit gọi 4 nguồn giá đồng thời bằng coroutine, làm mới tự động mỗi 3 giây và có nút làm mới thủ công.

Nguồn dữ liệu: CoinLore, Binance Spot public market data, CoinPaprika và Kraken. Giá dùng nguồn trả về được đầu tiên theo thứ tự Binance, Kraken, CoinPaprika, CoinLore. Nếu một nguồn lỗi, dữ liệu từ nguồn còn lại vẫn được dùng; nếu cả bốn lỗi, màn hình dùng dữ liệu minh họa. Các API công khai có thể áp dụng giới hạn tốc độ hoặc thay đổi quyền truy cập.

Mở thư mục này trong Android Studio, chọn cấu hình `app` rồi Run. Build APK debug bằng:

```powershell
.\gradlew.bat assembleDebug
```
