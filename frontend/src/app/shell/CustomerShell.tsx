import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { ShoppingCart, Package, LogOut, User, BookOpen } from "lucide-react";
import { useAuthStore } from "../../features/auth/authStore";
import { useCartStore, selectItemCount } from "../../features/customer/cart/cartStore";

export function CustomerShell() {
  const { username, logout } = useAuthStore();
  const navigate = useNavigate();
  const itemCount = useCartStore(selectItemCount);

  return (
    <div className="customer-shell">
      <header className="top-nav">
        <Link to="/customer/catalog" className="brand">
          <BookOpen size={22} />
          <span>Bookstore</span>
        </Link>

        <nav className="top-links">
          <NavLink to="/customer/catalog" className={({ isActive }) => (isActive ? "top-link active" : "top-link")}>
            Sản phẩm
          </NavLink>
          <NavLink to="/customer/orders" className={({ isActive }) => (isActive ? "top-link active" : "top-link")}>
            <Package size={15} /> Đơn hàng
          </NavLink>
        </nav>

        <div className="top-actions">
          <Link to="/customer/cart" className="cart-btn">
            <ShoppingCart size={20} />
            {itemCount > 0 && <span className="cart-badge">{itemCount}</span>}
          </Link>
          <div className="user-pill">
            <User size={15} />
            <span>{username}</span>
          </div>
          <button
            className="btn btn-ghost btn-sm"
            onClick={() => { logout(); navigate("/login"); }}
          >
            <LogOut size={15} />
          </button>
        </div>
      </header>

      <main className="customer-content">
        <Outlet />
      </main>
    </div>
  );
}
