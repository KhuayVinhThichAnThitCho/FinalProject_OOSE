import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import { api } from "../../../shared/api";
import { useAuthStore } from "../../auth/authStore";
import { useLoad } from "../../../shared/hooks/useLoad";
import { mapOrderDetailVM } from "../../../entities/order/mappers";
import { Card, EmptyState, ErrorBanner, StatusBadge, ConfirmDialog, Input, DataTable, Select } from "../../../shared/ui/components";
import { useToast } from "../../../shared/ui/toast";
import { getErrorMessage } from "../../../shared/lib/error";
import { formatCurrency } from "../../../shared/lib/format";
import { ArrowLeft, CreditCard, Loader2, XCircle } from "lucide-react";

const TIMELINE = ["PENDING", "PROCESSING", "PAID", "SHIPPING", "DELIVERED"];

function OrderTimeline({ current }: { current: string }) {
  const idx = TIMELINE.indexOf(current.toUpperCase());
  const isCancelled = current.toUpperCase() === "CANCELLED";
  return (
    <div className="order-timeline">
      {TIMELINE.map((s, i) => (
        <div key={s} className={`timeline-step ${i <= idx && !isCancelled ? "reached" : ""} ${i === idx && !isCancelled ? "current" : ""}`}>
          <div className="timeline-dot" />
          <span>{s}</span>
        </div>
      ))}
      {isCancelled && (
        <div className="timeline-step reached cancelled current">
          <div className="timeline-dot" />
          <span>CANCELLED</span>
        </div>
      )}
    </div>
  );
}

export default function OrderDetailPage() {
  const { id } = useParams();
  const orderId = id ?? "";
  const customerId = useAuthStore((s) => s.customerId) ?? 1;
  const [reloadKey, setReloadKey] = useState(0);
  const { data, loading, error } = useLoad(
    () => api.myOrderDetail(orderId, customerId),
    [orderId, customerId, reloadKey],
  );
  const [cancelOpen, setCancelOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState("");
  const [cancelling, setCancelling] = useState(false);
  const [mockOutcome, setMockOutcome] = useState("SUCCESS");
  const [paying, setPaying] = useState(false);
  const [paymentError, setPaymentError] = useState<string | null>(null);
  const { push } = useToast();

  if (loading) return <div className="skeleton-card" style={{ height: 200 }} />;
  if (error) return <ErrorBanner message={error} />;
  if (!data) return <EmptyState title="Không tìm thấy" desc="Đơn hàng không tồn tại." />;

  const vm = mapOrderDetailVM(data);
  const canCancel = !["SHIPPING", "DELIVERED", "CANCELLED"].includes(vm.status.toUpperCase());
  const st = vm.status.toUpperCase();
  const canRetryPayment =
    vm.items.length > 0 && (st === "PENDING" || st === "PROCESSING");

  const handleRetryPayment = async () => {
    setPaymentError(null);
    setPaying(true);
    try {
      await api.mockAuthorize(orderId, mockOutcome);
      const result = await api.checkout(orderId, "ONLINE");
      if (result.status === "PAID") {
        push(result.message || "Đặt hàng và Thanh toán thành công!", "success");
        setReloadKey((k) => k + 1);
      } else {
        push(result.message || "Thanh toán chưa thành công.", "error");
        setReloadKey((k) => k + 1);
      }
    } catch (e) {
      const msg = getErrorMessage(e, "Thanh toán thất bại. Vui lòng thử lại.");
      setPaymentError(msg);
      push(msg, "error");
    } finally {
      setPaying(false);
    }
  };

  return (
    <div className="order-detail-page">
      <Link to="/customer/orders" className="back-link">
        <ArrowLeft size={16} /> Quay lại danh sách
      </Link>

      <div className="order-detail-header">
        <div>
          <h2 className="order-heading-id">Đơn hàng #{String(vm.id)}</h2>
          <p className="muted">{vm.dateText}</p>
        </div>
        <StatusBadge status={vm.status} />
      </div>

      <OrderTimeline current={vm.status} />

      <div className="order-detail-grid">
        <Card>
          <h3>Sản phẩm</h3>
          <DataTable
            headers={["Tên sách", "Số lượng", "Đơn giá", "Thành tiền"]}
            rows={vm.items.map((i) => [
              i.title,
              i.quantity,
              i.unitPriceText,
              formatCurrency(i.unitPrice * i.quantity),
            ])}
          />
          <div className="summary-row total" style={{ marginTop: 12 }}>
            <span>Tổng cộng</span>
            <span>{vm.totalText}</span>
          </div>
        </Card>

        <Card>
          <h3>Thông tin giao hàng</h3>
          {vm.shipping ? (
            <>
              <p><strong>{vm.shipping.receiverName}</strong></p>
              <p>{vm.shipping.receiverPhone}</p>
              <p className="muted">{vm.shipping.address}</p>
            </>
          ) : (
            <p className="muted">Chưa có thông tin giao hàng</p>
          )}
        </Card>

        {canRetryPayment && (
          <Card>
            <h3>Thanh toán đơn hàng</h3>
            <p className="muted" style={{ marginBottom: 12 }}>
              Đơn đang chờ thanh toán online. Chọn kịch bản mock (nếu cần) rồi thử thanh toán lại.
            </p>
            {paymentError && <ErrorBanner message={paymentError} />}
            <div className="payment-single-online" style={{ marginBottom: 12 }}>
              <CreditCard size={20} />
              <div>
                <strong>Thanh toán trực tuyến (Online)</strong>
                <p className="muted">Chỉ hỗ trợ thanh toán qua cổng online.</p>
              </div>
            </div>
            <div className="form-group mock-section">
              <label>Kịch bản thanh toán (Mock Gateway)</label>
              <Select value={mockOutcome} onChange={(e) => setMockOutcome(e.target.value)}>
                <option value="SUCCESS">SUCCESS - Thành công</option>
                <option value="INSUFFICIENT_FUNDS">INSUFFICIENT_FUNDS - Không đủ số dư</option>
                <option value="MAINTENANCE">MAINTENANCE - Bảo trì</option>
              </Select>
            </div>
            <button type="button" className="btn btn-primary" disabled={paying} onClick={handleRetryPayment}>
              {paying ? (
                <>
                  <Loader2 size={16} className="spin" /> Đang xử lý...
                </>
              ) : (
                "Thanh toán"
              )}
            </button>
          </Card>
        )}

        {canCancel && (
          <Card>
            <h3>Yêu cầu hủy đơn</h3>
            <p className="muted" style={{ marginBottom: 8 }}>
              Bạn có thể yêu cầu hủy đơn trong vòng 24 giờ sau khi đặt hàng.
            </p>
            <button className="btn btn-danger" onClick={() => setCancelOpen(true)}>
              <XCircle size={16} /> Yêu cầu hủy
            </button>
          </Card>
        )}
      </div>

      <ConfirmDialog
        open={cancelOpen}
        title="Yêu cầu hủy đơn hàng"
        body={
          <div className="form-group">
            <label>Lý do hủy</label>
            <Input
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Nhập lý do hủy đơn..."
            />
          </div>
        }
        onClose={() => setCancelOpen(false)}
        onConfirm={async () => {
          if (!cancelReason.trim()) { push("Vui lòng nhập lý do hủy", "error"); return; }
          setCancelling(true);
          try {
            await api.createCancelRequest(orderId, cancelReason);
            push("Đã gửi yêu cầu hủy đơn. Nhân viên sẽ xem xét.", "success");
            setCancelOpen(false);
            setCancelReason("");
            setReloadKey((k) => k + 1);
          } catch (e) {
            push(getErrorMessage(e, "Gửi yêu cầu hủy thất bại"), "error");
          } finally {
            setCancelling(false);
          }
        }}
      />
      {cancelling && null}
    </div>
  );
}
