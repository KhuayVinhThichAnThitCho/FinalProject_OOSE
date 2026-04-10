import { Link, useNavigate } from "react-router-dom";
import { Trash2, Minus, Plus, ShoppingBag } from "lucide-react";
import { useCartStore, selectSubtotal } from "./cartStore";
import { formatCurrency } from "../../../shared/lib/format";
import { Card, EmptyState } from "../../../shared/ui/components";

export default function CartPage() {
  const items = useCartStore((s) => s.items);
  const updateQty = useCartStore((s) => s.updateQty);
  const removeItem = useCartStore((s) => s.removeItem);
  const subtotal = useCartStore(selectSubtotal);
  const navigate = useNavigate();

  if (items.length === 0) {
    return (
      <div className="cart-empty-wrap">
        <EmptyState
          title="Giỏ hàng trống"
          desc="Bạn chưa thêm sản phẩm nào. Hãy khám phá cửa hàng!"
        />
        <Link to="/customer/catalog" className="btn btn-primary" style={{ marginTop: 12 }}>
          Tiếp tục mua sắm
        </Link>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <h2>Giỏ hàng ({items.length} sản phẩm)</h2>

      <div className="cart-layout">
        <div className="cart-items">
          {items.map((item) => (
            <Card key={item.bookId} className="cart-item">
              <div className="cart-item-img" />
              <div className="cart-item-info">
                <h4>{item.title}</h4>
                <p className="cart-item-price">{formatCurrency(item.price)}</p>
              </div>
              <div className="cart-item-qty">
                <button className="qty-btn" onClick={() => updateQty(item.bookId, item.quantity - 1)}>
                  <Minus size={14} />
                </button>
                <span className="qty-value">{item.quantity}</span>
                <button className="qty-btn" onClick={() => updateQty(item.bookId, item.quantity + 1)}>
                  <Plus size={14} />
                </button>
              </div>
              <p className="cart-item-subtotal">{formatCurrency(item.price * item.quantity)}</p>
              <button className="btn btn-ghost btn-sm" onClick={() => removeItem(item.bookId)}>
                <Trash2 size={16} />
              </button>
            </Card>
          ))}
        </div>

        <Card className="cart-summary">
          <h3>Tóm tắt đơn hàng</h3>
          <div className="summary-row">
            <span>Tạm tính ({items.length} sản phẩm)</span>
            <span>{formatCurrency(subtotal)}</span>
          </div>
          <div className="summary-row">
            <span>Phí vận chuyển</span>
            <span className="muted">Tính ở bước tiếp theo</span>
          </div>
          <hr />
          <div className="summary-row total">
            <span>Tổng cộng</span>
            <span>{formatCurrency(subtotal)}</span>
          </div>
          <button
            className="btn btn-primary btn-full"
            onClick={() => navigate("/customer/checkout")}
          >
            <ShoppingBag size={16} /> Tiến hành thanh toán
          </button>
          <Link to="/customer/catalog" className="continue-link">
            ← Tiếp tục mua sắm
          </Link>
        </Card>
      </div>
    </div>
  );
}
