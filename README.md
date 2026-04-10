# FinalProject_OOSE - Commercial Bookstore

Du an gom 2 phan:
- Backend: Spring Boot + JPA + MySQL + JWT
- Frontend: React + Vite + TypeScript

README nay huong dan chay full du an va dang nhap theo tung role.

## 1) Yeu cau moi truong

- Java 17
- Maven 3.9+
- Node.js 20+ (khuyen nghi LTS)
- npm 10+
- Docker Desktop

## 2) Chay database MySQL

Tai thu muc goc project:

```bash
docker compose up -d
```

Thong tin DB:
- Host: `localhost`
- Port: `3306`
- Database: `bookstore`
- Username: `root`
- Password: `root`

> Backend dang doc config tu `src/main/resources/application.yml`.

## 3) Chay backend (Spring Boot)

Tai thu muc goc project:

```bash
mvn spring-boot:run
```

Backend se chay tai: [http://localhost:8080](http://localhost:8080)

Lan chay dau:
- Flyway tu dong tao schema
- Seed data user/books/payment methods tu migrations (`V2`, `V4`)

Swagger:
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 4) Chay frontend (React)

Mo terminal moi, vao thu muc frontend:

```bash
cd frontend
npm install
npm run dev
```

Frontend se chay tai: [http://localhost:5173](http://localhost:5173)

## 5) Dang nhap theo tung role

Tat ca mat khau dev deu la: `password`

| Role | Username | Password | Man hinh sau login |
|---|---|---|---|
| CUSTOMER | `customer` | `password` | Catalog / Cart / Checkout / My Orders |
| STAFF | `staff` | `password` | Order Queue / Cancel Queue |
| MANAGER | `manager` | `password` | Dashboard / Pricing / Reports |

Nguon seed credentials: `src/main/resources/db/migration/V2__seed.sql`.

## 6) API login (neu can test bang Postman/PowerShell)

Endpoint:
- `POST /api/auth/login`

Body:

```json
{
  "username": "customer",
  "password": "password"
}
```

PowerShell nhanh:

```powershell
$body = @{ username = "customer"; password = "password" } | ConvertTo-Json
$res = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -ContentType "application/json" -Body $body
$res
```

Sau khi login thanh cong, lay `token` va gan header:
- `Authorization: Bearer <token>`

## 7) Luong chuc nang chinh theo role

### CUSTOMER
- Xem catalog (`/api/books`)
- Them vao cart
- Checkout theo flow: tao order -> confirm order -> payment
- Xem danh sach don cua minh (`/api/orders/my`)
- Xem chi tiet don va gui yeu cau huy

### STAFF
- Xem don cho xu ly (`/api/staff/orders/pending`)
- Xac nhan don (`/api/staff/orders/{id}/confirm`)
- Xu ly queue yeu cau huy (`/api/cancel-requests/staff`)

### MANAGER
- Xem KPI dashboard
- Cap nhat gia sach (`/api/manager/books/{id}/price`)
- Xem/Export bao cao (`/api/reports/sales`, `/api/reports/sales/export`)

## 8) Lenh huu ich

Tai thu muc `frontend`:

```bash
npm run lint
npm run build
```

Tai thu muc goc project (backend):

```bash
mvn test
```

## 9) Troubleshooting nhanh

- Loi port 8080 dang bi dung:
  - Tat process dang chiem port 8080 hoac doi `server.port` trong `application.yml`
- Loi ket noi DB:
  - Kiem tra `docker compose ps`
  - Kiem tra username/password trong `application.yml`
- Frontend goi API loi CORS/network:
  - Dam bao backend dang chay truoc (`localhost:8080`)
