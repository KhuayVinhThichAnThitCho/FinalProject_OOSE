import { Link } from "react-router-dom";
import { Card } from "../../shared/ui/components";

export function UnauthorizedPage() {
  return (
    <div className="center-wrap">
      <Card className="auth-card">
        <h2 style={{ textAlign: "center" }}>Không có quyền truy cập</h2>
        <p className="muted" style={{ textAlign: "center" }}>Tài khoản của bạn không có quyền vào màn hình này.</p>
        <div className="row" style={{ justifyContent: "center", marginTop: 12 }}>
          <Link to="/" className="btn btn-primary">Về trang chủ</Link>
          <Link to="/login" className="btn">Đăng nhập lại</Link>
        </div>
      </Card>
    </div>
  );
}

export function NotFoundPage() {
  return (
    <div className="center-wrap">
      <Card className="auth-card">
        <h2 style={{ textAlign: "center" }}>404</h2>
        <p className="muted" style={{ textAlign: "center" }}>Trang bạn tìm kiếm không tồn tại.</p>
        <Link to="/" className="btn btn-primary" style={{ marginTop: 12 }}>Về trang chủ</Link>
      </Card>
    </div>
  );
}
