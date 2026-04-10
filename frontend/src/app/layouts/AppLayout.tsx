import { LogOut } from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../features/auth/authStore";
import type { Role } from "../../shared/types";

type MenuItem = { to: string; label: string; role: Role };

const menu: MenuItem[] = [
  { to: "/books", label: "Books", role: "CUSTOMER" },
  { to: "/orders/new", label: "Create Order", role: "CUSTOMER" },
  { to: "/orders/my", label: "Track Orders", role: "CUSTOMER" },
  { to: "/cancel-requests/new", label: "Cancel Request", role: "CUSTOMER" },
  { to: "/staff/orders/pending", label: "Pending Orders", role: "STAFF" },
  { to: "/staff/cancel-requests", label: "Cancel Queue", role: "STAFF" },
  { to: "/manager/books", label: "Book Pricing", role: "MANAGER" },
  { to: "/manager/reports/sales", label: "Sales Report", role: "MANAGER" },
];

export function AppLayout() {
  const { roles, username, logout } = useAuthStore();
  const navigate = useNavigate();
  const allowed = menu.filter((item) => roles.includes(item.role));

  return (
    <div className="shell">
      <aside className="sidebar">
        <h2>Bookstore Admin</h2>
        <p className="muted">{username}</p>
        <nav className="menu">
          {allowed.map((item) => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => `menu-item ${isActive ? "active" : ""}`}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <button
          className="btn btn-ghost"
          onClick={() => {
            logout();
            navigate("/login");
          }}
        >
          <LogOut size={16} />
          Logout
        </button>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
