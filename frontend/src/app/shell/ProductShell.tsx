import { NavLink, Outlet, useNavigate } from "react-router-dom";
import type { ReactNode } from "react";
import { LogOut, Package, UserCog, LayoutDashboard, DollarSign, BarChart3 } from "lucide-react";
import { useAuthStore } from "../../features/auth/authStore";
import type { Role } from "../../shared/types";

type Item = { to: string; label: string; roles: Role[]; icon: ReactNode };

const navItems: Item[] = [
  { to: "/staff/orders", label: "Đơn hàng chờ", roles: ["STAFF", "MANAGER"], icon: <Package size={16} /> },
  { to: "/staff/cancellations", label: "Yêu cầu hủy", roles: ["STAFF", "MANAGER"], icon: <UserCog size={16} /> },
  { to: "/manager/dashboard", label: "Dashboard", roles: ["MANAGER"], icon: <LayoutDashboard size={16} /> },
  { to: "/manager/pricing", label: "Cập nhật giá bán sách", roles: ["MANAGER"], icon: <DollarSign size={16} /> },
  { to: "/manager/reports", label: "Báo cáo", roles: ["MANAGER"], icon: <BarChart3 size={16} /> },
];

export function AdminShell() {
  const { roles, username, logout } = useAuthStore();
  const navigate = useNavigate();
  const visible = navItems.filter((i) => i.roles.some((r) => roles.includes(r)));

  const roleLabel = roles.includes("MANAGER") ? "Manager" : "Staff";

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="sidebar-brand">
          <h2>Bookstore</h2>
          <span className="role-tag">{roleLabel}</span>
        </div>
        <div className="sidebar-user">
          <span>{username}</span>
        </div>
        <nav className="admin-menu">
          {visible.map((i) => (
            <NavLink key={i.to} to={i.to} className={({ isActive }) => `admin-menu-item ${isActive ? "active" : ""}`}>
              {i.icon} {i.label}
            </NavLink>
          ))}
        </nav>
        <button
          className="btn btn-ghost sidebar-logout"
          onClick={() => { logout(); navigate("/login"); }}
        >
          <LogOut size={16} /> Đăng xuất
        </button>
      </aside>
      <main className="admin-content">
        <Outlet />
      </main>
    </div>
  );
}
