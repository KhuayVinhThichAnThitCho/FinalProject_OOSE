import { useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { mapOrderDetailVM } from "../../../entities/order/mappers";
import { Card, EmptyState, ErrorBanner, StatusBadge, DataTable, ConfirmDialog } from "../../../shared/ui/components";
import { useToast } from "../../../shared/ui/toast";
import { getErrorMessage } from "../../../shared/lib/error";
import { formatCurrency } from "../../../shared/lib/format";
import { ArrowLeft, Truck, Ban } from "lucide-react";

export default function OrderWorkspaceDetailPage() {
  const { id } = useParams();
  const orderId = id ?? "";
  const navigate = useNavigate();
  const [reloadKey, setReloadKey] = useState(0);
  const { data, loading, error } = useLoad(() => api.staffOrderDetail(orderId), [orderId, reloadKey]);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [cancelConfirmOpen, setCancelConfirmOpen] = useState(false);
  const { push } = useToast();

  if (loading) return <Card>Đang tải chi tiết...</Card>;
  if (error) return <ErrorBanner message={error} />;
  if (!data) return <EmptyState title="Không có dữ liệu" desc="Không tìm thấy đơn hàng." />;
  const vm = mapOrderDetailVM(data);

  const canConfirm = vm.status.toUpperCase() === "PAID";

  return (
    <div className="page">
      <Link to="/staff/orders" className="back-link">
        <ArrowLeft size={16} /> Quay lại hàng đợi
      </Link>

      <div className="page-header">
        <div>
          <h2 className="order-heading-id">Đơn hàng #{String(vm.id)}</h2>
          <p className="muted">{vm.dateText}</p>
        </div>
        <div className="row staff-order-actions">
          <StatusBadge status={vm.status} />
          {canConfirm && (
            <>
              <button type="button" className="btn" onClick={() => setCancelConfirmOpen(true)}>
                <Ban size={16} /> Hủy xác nhận
              </button>
              <button type="button" className="btn btn-primary" onClick={() => setConfirmOpen(true)}>
                <Truck size={16} /> Xác nhận giao hàng
              </button>
            </>
          )}
        </div>
      </div>

      <div className="grid two">
        <Card>
          <h3>Sản phẩm ({vm.items.length})</h3>
          <DataTable
            headers={["Tên sách", "SL", "Đơn giá", "Thành tiền"]}
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
      </div>

      <ConfirmDialog
        open={confirmOpen}
        title="Xác nhận giao hàng?"
        body={<p>Đơn hàng sẽ chuyển sang trạng thái SHIPPING. Hành động này không thể hoàn tác.</p>}
        onClose={() => setConfirmOpen(false)}
        onConfirm={async () => {
          try {
            const msg = await api.staffConfirmOrder(vm.id);
            push(msg, "success");
            setReloadKey((k) => k + 1);
          } catch (e) {
            push(getErrorMessage(e, "Xác nhận thất bại"), "error");
          } finally {
            setConfirmOpen(false);
          }
        }}
      />

      <ConfirmDialog
        open={cancelConfirmOpen}
        title="Hủy xác nhận đơn hàng?"
        body={
          <p>
            Hệ thống <strong>giữ nguyên</strong> trạng thái đơn hàng. Bạn chỉ thoát thao tác xác nhận giao hàng trên màn hình này.
          </p>
        }
        onClose={() => setCancelConfirmOpen(false)}
        onConfirm={async () => {
          try {
            const msg = await api.staffCancelOrderConfirmation(vm.id);
            push(typeof msg === "string" ? msg : "Đã hủy xác nhận đơn hàng!", "info");
            navigate("/staff/orders");
          } catch (e) {
            push(getErrorMessage(e, "Thao tác thất bại"), "error");
          } finally {
            setCancelConfirmOpen(false);
          }
        }}
      />
    </div>
  );
}
