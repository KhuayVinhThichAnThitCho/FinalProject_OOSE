import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { mapOrderSummaryVM } from "../../../entities/order/mappers";
import { Card, DataTable, EmptyState, ErrorBanner, PageHeader, StatusBadge, ConfirmDialog } from "../../../shared/ui/components";
import { useToast } from "../../../shared/ui/toast";
import { getErrorMessage } from "../../../shared/lib/error";
import { Truck, Eye, PackageCheck } from "lucide-react";
import type { IdLike } from "../../../shared/types";

type Tab = "pending" | "shipping";

export default function OrderQueuePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const tab: Tab = searchParams.get("tab") === "shipping" ? "shipping" : "pending";
  const setTab = (t: Tab) => {
    if (t === "pending") setSearchParams({});
    else setSearchParams({ tab: "shipping" });
  };

  const loader = tab === "pending" ? api.staffPendingOrders : api.staffShippingOrders;
  const { data, loading, error, setData } = useLoad(loader, [tab]);
  const { push } = useToast();
  const [confirmShipId, setConfirmShipId] = useState<IdLike | null>(null);
  const [confirmDeliverId, setConfirmDeliverId] = useState<IdLike | null>(null);

  if (loading) return <Card>Đang tải đơn hàng...</Card>;
  if (error) return <ErrorBanner message={error} />;

  const emptyTitle = tab === "pending" ? "Hàng đợi trống" : "Không có đơn đang giao";
  const emptyDesc =
    tab === "pending"
      ? "Hiện chưa có đơn chờ xác nhận giao hàng."
      : "Chưa có đơn ở trạng thái đang giao (SHIPPING).";

  if (!data || data.length === 0) {
    return (
      <div className="page">
        <div className="staff-order-tabs">
          <button type="button" className={`tab-btn ${tab === "pending" ? "active" : ""}`} onClick={() => setTab("pending")}>
            Chờ xác nhận giao
          </button>
          <button type="button" className={`tab-btn ${tab === "shipping" ? "active" : ""}`} onClick={() => setTab("shipping")}>
            Đang giao hàng
          </button>
        </div>
        <EmptyState title={emptyTitle} desc={emptyDesc} />
      </div>
    );
  }

  return (
    <div className="page">
      <div className="staff-order-tabs">
        <button type="button" className={`tab-btn ${tab === "pending" ? "active" : ""}`} onClick={() => setTab("pending")}>
          Chờ xác nhận giao
        </button>
        <button type="button" className={`tab-btn ${tab === "shipping" ? "active" : ""}`} onClick={() => setTab("shipping")}>
          Đang giao hàng
        </button>
      </div>

      <PageHeader
        title={tab === "pending" ? "Đơn hàng chờ xử lý" : "Đơn đang giao hàng"}
        subtitle={
          tab === "pending"
            ? `${data.length} đơn đang chờ xác nhận giao hàng (PAID)`
            : `${data.length} đơn đang giao — xác nhận khi khách đã nhận hàng`
        }
      />

      <DataTable
        headers={["Mã đơn", "Ngày đặt", "Tổng tiền", "Trạng thái", "Thao tác"]}
        rows={data.map((o) => {
          const vm = mapOrderSummaryVM(o);
          return [
            String(vm.id),
            vm.dateText,
            vm.totalText,
            <StatusBadge key="s" status={vm.status} />,
            <div key="a" className="row">
              <Link to={`/staff/orders/${vm.id}`} className="btn btn-sm">
                <Eye size={14} /> Chi tiết
              </Link>
              {tab === "pending" ? (
                <button type="button" className="btn btn-primary btn-sm" onClick={() => setConfirmShipId(vm.id)}>
                  <Truck size={14} /> Xác nhận giao
                </button>
              ) : (
                <button type="button" className="btn btn-primary btn-sm" onClick={() => setConfirmDeliverId(vm.id)}>
                  <PackageCheck size={14} /> Đã giao
                </button>
              )}
            </div>,
          ];
        })}
      />

      <ConfirmDialog
        open={confirmShipId !== null}
        title="Xác nhận giao hàng?"
        body={<p>Đơn hàng sẽ chuyển sang trạng thái SHIPPING.</p>}
        onClose={() => setConfirmShipId(null)}
        onConfirm={async () => {
          if (!confirmShipId) return;
          try {
            const msg = await api.staffConfirmOrder(confirmShipId);
            push(msg, "success");
            setData(await api.staffPendingOrders());
          } catch (e) {
            push(getErrorMessage(e, "Xác nhận đơn thất bại"), "error");
          } finally {
            setConfirmShipId(null);
          }
        }}
      />

      <ConfirmDialog
        open={confirmDeliverId !== null}
        title="Xác nhận đã giao thành công?"
        body={<p>Đơn hàng sẽ chuyển sang trạng thái DELIVERED. Chỉ xác nhận khi khách đã nhận được hàng.</p>}
        onClose={() => setConfirmDeliverId(null)}
        onConfirm={async () => {
          if (!confirmDeliverId) return;
          try {
            const msg = await api.staffMarkDelivered(confirmDeliverId);
            push(msg, "success");
            setData(await api.staffShippingOrders());
          } catch (e) {
            push(getErrorMessage(e, "Cập nhật trạng thái thất bại"), "error");
          } finally {
            setConfirmDeliverId(null);
          }
        }}
      />
    </div>
  );
}
