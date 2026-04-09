# FinalProject_OOSE — Bookstore Backend (Spring Boot + JPA + MySQL + JWT)

Backend REST API cho hệ thống nhà sách theo đặc tả OOSE:
- Đặt hàng + thanh toán (mock payment gateway)
- Theo dõi đơn hàng
- Xử lý đơn hàng (staff/manager)
- Xử lý yêu cầu hủy đơn
- Cập nhật giá bán sách (có cảnh báo bán lỗ)
- Báo cáo bán hàng + export XLSX/PDF
- JWT Security + JPA Auditing

## Yêu cầu
- **Java 17**
- **Maven** (hoặc dùng `mvnw` nếu bạn tự thêm wrapper)
- **Docker Desktop** (để chạy MySQL bằng Docker Compose)

## Chạy MySQL bằng Docker
Tại thư mục project:

```bash
docker compose up -d
```

MySQL sẽ chạy ở `localhost:3306`, database `bookstore`.

## Cấu hình DB
File cấu hình: `src/main/resources/application.yml`

Mặc định backend dùng:
- URL: `jdbc:mysql://localhost:3306/bookstore?...`
- user/pass: `root/root`

## Chạy backend

```bash
mvn spring-boot:run
```

Khi chạy lần đầu, **Flyway** sẽ tự tạo schema và seed dữ liệu.

## Swagger UI
Mở trình duyệt:
- `http://localhost:8080/swagger-ui/index.html`

## Đăng nhập (JWT)
Endpoint:
- `POST /api/auth/login`

Tài khoản seed (dev):
- **customer / password** (ROLE_CUSTOMER)
- **staff / password** (ROLE_STAFF)
- **manager / password** (ROLE_MANAGER)

### Test nhanh bằng PowerShell

```powershell
$body = @{ username = "customer"; password = "password" } | ConvertTo-Json
$res = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -ContentType "application/json" -Body $body
$token = $res.token

# gọi API cần auth (ví dụ: list orders của customer khachHangId=1)
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/orders?khachHangId=1" -Headers $headers
```

## Luồng API chính (tóm tắt)

### 1) Checkout (đặt hàng + thanh toán)
- `POST /api/orders/checkout` (ROLE_CUSTOMER)

Body mẫu:

```json
{
  "khachHangId": 1,
  "items": [
    { "sachId": 1, "soLuong": 2 }
  ],
  "nguoiNhan": "Nguyen Van A",
  "soDienThoaiNhan": "0900000000",
  "diaChiGiaoHang": "HCM",
  "paymentMethodCode": "ONLINE_OK"
}
```

Mock payment behavior theo `paymentMethodCode`:
- `ONLINE_OK` → thành công
- `ONLINE_NO_MONEY` → thiếu số dư
- `ONLINE_MAINT` → bảo trì
- `ONLINE_CANCEL` → người dùng hủy

### 2) Theo dõi đơn hàng
- `GET /api/orders?khachHangId=...` (ROLE_CUSTOMER)
- `GET /api/orders/{id}?khachHangId=...` (ROLE_CUSTOMER)

### 3) Staff xử lý đơn
- `GET /api/staff/orders/pending` (ROLE_STAFF/ROLE_MANAGER)
- `POST /api/staff/orders/{id}/confirm` (ROLE_STAFF/ROLE_MANAGER)

### 4) Yêu cầu hủy đơn
- `POST /api/cancel-requests` (ROLE_CUSTOMER)
- `GET /api/cancel-requests/staff` (ROLE_STAFF/ROLE_MANAGER)
- `POST /api/cancel-requests/{id}/approve` (ROLE_STAFF/ROLE_MANAGER)
- `POST /api/cancel-requests/{id}/reject` (ROLE_STAFF/ROLE_MANAGER)

### 5) Manager cập nhật giá
- `PUT /api/manager/books/{id}/price` (ROLE_MANAGER)

### 6) Báo cáo bán hàng
- `GET /api/reports/sales?from=...&to=...` (ROLE_MANAGER)
- `GET /api/reports/sales/export?from=...&to=...&format=xlsx|pdf` (ROLE_MANAGER)

`from/to` dùng định dạng ISO-8601, ví dụ:
- `2026-01-01T00:00:00Z`

## Chạy test

```bash
mvn test
```

Test dùng H2 in-memory (không cần MySQL).

