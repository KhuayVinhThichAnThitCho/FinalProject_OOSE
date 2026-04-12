import { Link } from "react-router-dom";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { DataTable, EmptyState, ErrorBanner, PageHeader, Select, StatusBadge } from "../../../shared/ui/components";
import { useState } from "react";

export default function CancelQueuePage() {
  const [status, setStatus] = useState("");
  const { data, loading, error } = useLoad(() => api.staffCancelRequests(status || undefined), [status]);
  const rows = Array.isArray(data) ? data : [];
  if (loading) return <div className="card">Đang tải yêu cầu hủy...</div>;
  if (error) return <ErrorBanner message={error} />;
  if (rows.length === 0) return <EmptyState title="Không có yêu cầu hủy" desc="Queue hiện đang trống." />;

  return (
    <div className="page">
      <PageHeader
        title="Cancel Queue"
        subtitle="Xử lý yêu cầu hủy theo trạng thái"
        actions={
          <Select value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">All</option>
            <option value="PENDING">PENDING</option>
            <option value="APPROVED">APPROVED</option>
            <option value="REJECTED">REJECTED</option>
          </Select>
        }
      />
      <DataTable
        headers={["Request", "Order", "Lý do", "Trạng thái", "Chi tiết"]}
        rows={rows.map((r) => [
          `#${r.id}`,
          `#${r.order?.id ?? "-"}`,
          r.reason,
          <StatusBadge status={r.status} />,
          <Link to={`/staff/cancellations/${r.id}`}>Mở</Link>,
        ])}
      />
    </div>
  );
}
