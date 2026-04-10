import { useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { mapOrderSummaryVM } from "../../../entities/order/mappers";
import { Card, DataTable, EmptyState, ErrorBanner, PageHeader, StatusBadge, ConfirmDialog } from "../../../shared/ui/components";
import { useToast } from "../../../shared/ui/toast";
import { getErrorMessage } from "../../../shared/lib/error";
import { Truck, Eye } from "lucide-react";
import type { IdLike } from "../../../shared/types";

export default function OrderQueuePage() {
  const { data, loading, error, setData } = useLoad(api.staffPendingOrders, []);
  const { push } = useToast();
  const [confirmId, setConfirmId] = useState<IdLike | null>(null);

  if (loading) return <Card>Đang tải hàng đợi...</Card>;
  if (error) return <ErrorBanner message={error} />;
  if (!data || data.length === 0) return <EmptyState title="Hàng đợi trống" desc="Hiện chưa có đơn cần xử lý." />;

  return (
    <div className="page">
      <PageHeader title="Đơn hàng chờ xử lý" subtitle={`${data.length} đơn đang chờ xác nhận giao hàng`} />
      <DataTable
        headers={["Mã đơn", "Ngày đặt", "Tổng tiền", "Trạng thái", "Thao tác"]}
        rows={data.map((o) => {
          const vm = mapOrderSummaryVM(o);
          return [
            `#${String(vm.id).slice(-8)}`,
            vm.dateText,
            vm.totalText,
            <StatusBadge status={vm.status} />,
            <div className="row">
              <Link to={`/staff/orders/${vm.id}`} className="btn btn-sm">
                <Eye size={14} /> Chi tiết
              </Link>
              <button className="btn btn-primary btn-sm" onClick={() => setConfirmId(vm.id)}>
                <Truck size={14} /> Xác nhận
              </button>
            </div>,
          ];
        })}
      />

      <ConfirmDialog
        open={confirmId !== null}
        title="Xác nhận giao hàng?"
        body={<p>Đơn hàng sẽ chuyển sang trạng thái SHIPPING.</p>}
        onClose={() => setConfirmId(null)}
        onConfirm={async () => {
          if (!confirmId) return;
          try {
            const msg = await api.staffConfirmOrder(confirmId);
            push(msg, "success");
            const refreshed = await api.staffPendingOrders();
            setData(refreshed);
          } catch (e) {
            push(getErrorMessage(e, "Xác nhận đơn thất bại"), "error");
          } finally {
            setConfirmId(null);
          }
        }}
      />
    </div>
  );
}
