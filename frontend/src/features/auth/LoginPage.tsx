import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "./authStore";
import { api } from "../../shared/api";
import { getErrorMessage } from "../../shared/lib/error";
import { Button, Card, Input } from "../../shared/ui/components";
import { useToast } from "../../shared/ui/toast";

export default function LoginPage() {
  const [username, setUsername] = useState("customer");
  const [password, setPassword] = useState("password");
  const [loading, setLoading] = useState(false);
  const setAuth = useAuthStore((s) => s.setAuth);
  const roles = useAuthStore((s) => s.roles);
  const navigate = useNavigate();
  const { push } = useToast();

  useEffect(() => {
    if (roles.length > 0) navigate("/");
  }, [navigate, roles.length]);

  return (
    <div className="center-wrap">
      <Card className="auth-card">
        <h1>Bookstore Portal</h1>
        <p className="muted">Đăng nhập để truy cập không gian làm việc</p>
        <label>Username</label>
        <Input value={username} onChange={(e) => setUsername(e.target.value)} />
        <label>Password</label>
        <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <Button
          className="btn-primary"
          disabled={loading}
          onClick={async () => {
            setLoading(true);
            try {
              const res = await api.login(username, password);
              setAuth(res);
              push("Đăng nhập thành công", "success");
              navigate("/");
            } catch (e) {
              push(getErrorMessage(e, "Đăng nhập thất bại"), "error");
            } finally {
              setLoading(false);
            }
          }}
        >
          {loading ? "Đang đăng nhập..." : "Đăng nhập"}
        </Button>
      </Card>
    </div>
  );
}
