import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../../../shared/api";
import { useCartStore, selectSubtotal } from "../cart/cartStore";
import { useAuthStore } from "../../auth/authStore";
import { useToast } from "../../../shared/ui/toast";
import { getErrorMessage } from "../../../shared/lib/error";
import { formatCurrency } from "../../../shared/lib/format";
import { Card, Input, Select, ErrorBanner } from "../../../shared/ui/components";
import { ChevronLeft, ChevronRight, Loader2, CheckCircle2, MapPin, CreditCard, ClipboardList } from "lucide-react";

type Step = 1 | 2 | 3;

const STEPS: { num: Step; label: string; icon: React.ReactNode }[] = [
  { num: 1, label: "Giao hàng", icon: <MapPin size={16} /> },
  { num: 2, label: "Thanh toán", icon: <CreditCard size={16} /> },
  { num: 3, label: "Xác nhận", icon: <ClipboardList size={16} /> },
];

export default function CheckoutPage() {
  const items = useCartStore((s) => s.items);
  const clearCart = useCartStore((s) => s.clear);
  const subtotal = useCartStore(selectSubtotal);
  const customerId = useAuthStore((s) => s.customerId);
  const navigate = useNavigate();
  const { push } = useToast();

  const [step, setStep] = useState<Step>(1);
  const [receiverName, setReceiverName] = useState("");
  const [receiverPhone, setReceiverPhone] = useState("");
  const [shippingAddress, setShippingAddress] = useState("");
  const [shippingFee, setShippingFee] = useState(15000);
  const [paymentMethod, setPaymentMethod] = useState("ONLINE");
  const [mockOutcome, setMockOutcome] = useState("SUCCESS");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const total = subtotal + shippingFee;

  if (items.length === 0) {
    return (
      <div className="checkout-empty">
        <Card>
          <h2>Giỏ hàng trống</h2>
          <p className="muted">Bạn cần thêm sản phẩm trước khi thanh toán.</p>
          <button className="btn btn-primary" onClick={() => navigate("/customer/catalog")}>
            Quay về cửa hàng
          </button>
        </Card>
      </div>
    );
  }

  const canNextStep1 = receiverName.trim() && receiverPhone.trim() && shippingAddress.trim();

  const handlePlaceOrder = async () => {
    if (!customerId) {
      setError("Không xác định được tài khoản khách hàng. Vui lòng đăng nhập lại.");
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const created = await api.createOrder(customerId);
      const orderId = created.orderId;

      await api.confirmOrder(orderId, {
        items: items.map((i) => ({ bookId: i.bookId, quantity: i.quantity })),
        receiverName,
        receiverPhone,
        shippingAddress,
        shippingFee,
      });

      if (paymentMethod === "ONLINE") {
        await api.mockAuthorize(orderId, mockOutcome);
      }

      const result = await api.checkout(orderId, paymentMethod);

      clearCart();
      push(result.message || "Đặt hàng thành công!", result.status === "PAID" ? "success" : "info");
      navigate(`/customer/orders/${orderId}`);
    } catch (e) {
      setError(getErrorMessage(e, "Đặt hàng thất bại. Vui lòng thử lại."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="checkout-page">
      <div className="wizard-steps">
        {STEPS.map((s) => (
          <div key={s.num} className={`wizard-step ${step === s.num ? "active" : ""} ${step > s.num ? "done" : ""}`}>
            <div className="step-circle">
              {step > s.num ? <CheckCircle2 size={18} /> : <span>{s.num}</span>}
            </div>
            <span className="step-label">{s.icon} {s.label}</span>
          </div>
        ))}
      </div>

      {error && <ErrorBanner message={error} />}

      {step === 1 && (
        <Card className="checkout-card">
          <h3>Thông tin giao hàng</h3>
          <div className="form-group">
            <label>Họ tên người nhận</label>
            <Input value={receiverName} onChange={(e) => setReceiverName(e.target.value)} placeholder="Nguyễn Văn A" />
          </div>
          <div className="form-group">
            <label>Số điện thoại</label>
            <Input value={receiverPhone} onChange={(e) => setReceiverPhone(e.target.value)} placeholder="0900 000 000" />
          </div>
          <div className="form-group">
            <label>Địa chỉ giao hàng</label>
            <Input value={shippingAddress} onChange={(e) => setShippingAddress(e.target.value)} placeholder="123 Nguyễn Huệ, Q1, TP.HCM" />
          </div>
          <div className="form-group">
            <label>Phí vận chuyển</label>
            <Input type="number" min={0} value={shippingFee} onChange={(e) => setShippingFee(Number(e.target.value))} />
          </div>
          <div className="wizard-nav">
            <button className="btn" onClick={() => navigate("/customer/cart")}>
              <ChevronLeft size={16} /> Quay lại giỏ hàng
            </button>
            <button className="btn btn-primary" disabled={!canNextStep1} onClick={() => setStep(2)}>
              Tiếp tục <ChevronRight size={16} />
            </button>
          </div>
        </Card>
      )}

      {step === 2 && (
        <Card className="checkout-card">
          <h3>Phương thức thanh toán</h3>
          <div className="payment-options">
            <label className={`payment-option ${paymentMethod === "ONLINE" ? "selected" : ""}`}>
              <input type="radio" name="pm" value="ONLINE" checked={paymentMethod === "ONLINE"} onChange={() => setPaymentMethod("ONLINE")} />
              <CreditCard size={20} />
              <div>
                <strong>Thanh toán Online</strong>
                <p className="muted">Thanh toán trực tuyến ngay lập tức</p>
              </div>
            </label>
            <label className={`payment-option ${paymentMethod === "COD" ? "selected" : ""}`}>
              <input type="radio" name="pm" value="COD" checked={paymentMethod === "COD"} onChange={() => setPaymentMethod("COD")} />
              <MapPin size={20} />
              <div>
                <strong>Thanh toán khi nhận hàng (COD)</strong>
                <p className="muted">Trả tiền khi nhận được sách</p>
              </div>
            </label>
          </div>

          {paymentMethod === "ONLINE" && (
            <div className="form-group mock-section">
              <label>Kịch bản thanh toán (Mock Gateway)</label>
              <Select value={mockOutcome} onChange={(e) => setMockOutcome(e.target.value)}>
                <option value="SUCCESS">SUCCESS - Thành công</option>
                <option value="INSUFFICIENT_FUNDS">INSUFFICIENT_FUNDS - Không đủ số dư</option>
                <option value="USER_CANCELLED">USER_CANCELLED - Người dùng hủy</option>
                <option value="MAINTENANCE">MAINTENANCE - Bảo trì</option>
              </Select>
            </div>
          )}

          <div className="wizard-nav">
            <button className="btn" onClick={() => setStep(1)}>
              <ChevronLeft size={16} /> Quay lại
            </button>
            <button className="btn btn-primary" onClick={() => setStep(3)}>
              Tiếp tục <ChevronRight size={16} />
            </button>
          </div>
        </Card>
      )}

      {step === 3 && (
        <div className="review-layout">
          <div className="review-main">
            <Card className="checkout-card">
              <h3>Xác nhận đơn hàng</h3>

              <div className="review-section">
                <h4>Sản phẩm ({items.length})</h4>
                {items.map((it) => (
                  <div key={it.bookId} className="review-item">
                    <span>{it.title} × {it.quantity}</span>
                    <span>{formatCurrency(it.price * it.quantity)}</span>
                  </div>
                ))}
              </div>

              <div className="review-section">
                <h4>Giao hàng</h4>
                <p>{receiverName} - {receiverPhone}</p>
                <p className="muted">{shippingAddress}</p>
              </div>

              <div className="review-section">
                <h4>Thanh toán</h4>
                <p>{paymentMethod === "ONLINE" ? "Thanh toán Online" : "Thanh toán khi nhận hàng"}</p>
              </div>
            </Card>
          </div>

          <Card className="order-total-card">
            <h3>Tổng thanh toán</h3>
            <div className="summary-row">
              <span>Tạm tính</span>
              <span>{formatCurrency(subtotal)}</span>
            </div>
            <div className="summary-row">
              <span>Phí vận chuyển</span>
              <span>{formatCurrency(shippingFee)}</span>
            </div>
            <hr />
            <div className="summary-row total">
              <span>Tổng cộng</span>
              <span>{formatCurrency(total)}</span>
            </div>

            <div className="wizard-nav vertical">
              <button
                className="btn btn-primary btn-full"
                disabled={submitting}
                onClick={handlePlaceOrder}
              >
                {submitting ? <><Loader2 size={16} className="spin" /> Đang xử lý...</> : "Đặt hàng"}
              </button>
              <button className="btn btn-full" onClick={() => setStep(2)}>
                <ChevronLeft size={16} /> Quay lại
              </button>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}
