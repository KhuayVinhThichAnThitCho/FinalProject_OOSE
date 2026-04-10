import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { Card, EmptyState, ErrorBanner, StatusBadge, ConfirmDialog, DataTable } from "../../../shared/ui/components";
import { useToast } from "../../../shared/ui/toast";
import { getErrorMessage } from "../../../shared/lib/error";
import { formatCurrency, formatDateTime } from "../../../shared/lib/format";
import { ArrowLeft, CheckCircle, XCircle } from "lucide-react";

export default function CancelDetailPage() {
  const { id } = useParams();
  const reqId = Number(id ?? 0);
  const [reloadKey, setReloadKey] = useState(0);
  const { data, loading, error } = useLoad(() => api.staffCancelDetail(reqId), [reqId, reloadKey]);
  const [openApprove, setOpenApprove] = useState(false);
  const [openReject, setOpenReject] = useState(false);
  const { push } = useToast();

  if (loading) return <Card>Đang tải yêu cầu...</Card>;
  if (error) return <ErrorBanner message={error} />;
  if (!data) return <EmptyState title="Không tìm thấy" desc="Yêu cầu hủy không tồn tại." />;

  const isPending = data.status.toUpperCase() === "PENDING";
  const od = data.orderDetail;

  return (
    <div className="page">
      <Link to="/staff/cancellations" className="back-link">
        <ArrowLeft size={16} /> Quay lại hàng đợi
      </Link>

      <div className="page-header">
        <div>
          <h2>Yêu cầu hủy #{data.cancelRequestId}</h2>
          <p className="muted">Gửi lúc {formatDateTime(data.requestedAt)}</p>
        </div>
        <StatusBadge status={data.status} />
      </div>

      <Card>
        <h3>Lý do hủy</h3>
        <p>{data.reason}</p>
      </Card>

      <div className="grid two">
        <Card>
          <h3>Đơn hàng #{String(od.orderId).slice(-8)}</h3>
          <div className="row" style={{ marginBottom: 8 }}>
            <StatusBadge status={od.status} />
            <span className="muted">{formatDateTime(od.ngayDat)}</span>
          </div>
          <DataTable
            headers={["Sản phẩm", "SL", "Đơn giá", "Thành tiền"]}
            rows={od.items.map((i) => [
              i.title,
              i.quantity,
              formatCurrency(i.unitPrice),
              formatCurrency(i.unitPrice * i.quantity),
            ])}
          />
          <div className="summary-row total" style={{ marginTop: 8 }}>
            <span>Tổng cộng</span>
            <span>{formatCurrency(od.totalAmount)}</span>
          </div>
        </Card>

        <Card>
          <h3>Thông tin giao hàng</h3>
          {od.shipping ? (
            <>
              <p><strong>{od.shipping.receiverName}</strong></p>
              <p>{od.shipping.receiverPhone}</p>
              <p className="muted">{od.shipping.address}</p>
            </>
          ) : (
            <p className="muted">Chưa có thông tin giao hàng</p>
          )}
        </Card>
      </div>

      {isPending && (
        <div className="row" style={{ marginTop: 16 }}>
          <button className="btn btn-primary" onClick={() => setOpenApprove(true)}>
            <CheckCircle size={16} /> Duyệt hủy
          </button>
          <button className="btn btn-danger" onClick={() => setOpenReject(true)}>
            <XCircle size={16} /> Từ chối
          </button>
        </div>
      )}

      <ConfirmDialog
        open={openApprove}
        title="Duyệt yêu cầu hủy?"
        body={<p>Đơn hàng sẽ bị hủy và không thể khôi phục.</p>}
        onClose={() => setOpenApprove(false)}
        onConfirm={async () => {
          try {
            const msg = await api.approveCancel(reqId);
            push(msg, "success");
            setReloadKey((k) => k + 1);
          } catch (e) {
            push(getErrorMessage(e, "Duyệt thất bại"), "error");
          } finally {
            setOpenApprove(false);
          }
        }}
      />
      <ConfirmDialog
        open={openReject}
        title="Từ chối yêu cầu hủy?"
        body={<p>Yêu cầu hủy sẽ bị từ chối, đơn hàng giữ nguyên trạng thái.</p>}
        onClose={() => setOpenReject(false)}
        onConfirm={async () => {
          try {
            const msg = await api.rejectCancel(reqId);
            push(msg, "info");
            setReloadKey((k) => k + 1);
          } catch (e) {
            push(getErrorMessage(e, "Từ chối thất bại"), "error");
          } finally {
            setOpenReject(false);
          }
        }}
      />
    </div>
  );
}
