import { useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { mapOrderSummaryVM } from "../../../entities/order/mappers";
import { Card, EmptyState, ErrorBanner, StatusBadge } from "../../../shared/ui/components";
import { ChevronLeft, ChevronRight, Eye } from "lucide-react";

export default function OrdersPage() {
  const { data, loading, error } = useLoad(api.myOrders, []);
  const [page, setPage] = useState(1);
  const perPage = 6;

  if (loading) {
    return (
      <div className="orders-skeleton">
        {Array.from({ length: 4 }).map((_, i) => <div key={i} className="skeleton-card" />)}
      </div>
    );
  }

  if (error) return <ErrorBanner message={error} />;

  const all = data?.orders ?? [];
  if (all.length === 0) {
    return (
      <div className="page">
        <EmptyState title="Chưa có đơn hàng" desc={data?.message ?? "Bạn chưa đặt đơn nào. Hãy bắt đầu mua sắm!"} />
        <Link to="/customer/catalog" className="btn btn-primary" style={{ marginTop: 12 }}>
          Khám phá sản phẩm
        </Link>
      </div>
    );
  }

  const totalPages = Math.ceil(all.length / perPage);
  const current = all.slice((page - 1) * perPage, page * perPage);

  return (
    <div className="orders-page">
      <h2>Đơn hàng của tôi</h2>
      <p className="muted" style={{ marginBottom: 16 }}>Bạn có {all.length} đơn hàng</p>

      <div className="order-list">
        {current.map((o) => {
          const vm = mapOrderSummaryVM(o);
          return (
            <Card key={String(vm.id)} className="order-card">
              <div className="order-card-header">
                <span className="order-id">#{String(vm.id)}</span>
                <StatusBadge status={vm.status} />
              </div>
              <div className="order-card-body">
                <div className="order-meta">
                  <span className="muted">Ngày đặt</span>
                  <span>{vm.dateText}</span>
                </div>
                <div className="order-meta">
                  <span className="muted">Tổng tiền</span>
                  <span className="order-total">{vm.totalText}</span>
                </div>
              </div>
              <Link to={`/customer/orders/${vm.id}`} className="btn btn-sm order-detail-link">
                <Eye size={14} /> Xem chi tiết
              </Link>
            </Card>
          );
        })}
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button className="btn btn-sm" disabled={page <= 1} onClick={() => setPage((p) => p - 1)}>
            <ChevronLeft size={14} />
          </button>
          {Array.from({ length: totalPages }).map((_, i) => (
            <button
              key={i}
              className={`btn btn-sm ${page === i + 1 ? "btn-primary" : ""}`}
              onClick={() => setPage(i + 1)}
            >
              {i + 1}
            </button>
          ))}
          <button className="btn btn-sm" disabled={page >= totalPages} onClick={() => setPage((p) => p + 1)}>
            <ChevronRight size={14} />
          </button>
        </div>
      )}
    </div>
  );
}
