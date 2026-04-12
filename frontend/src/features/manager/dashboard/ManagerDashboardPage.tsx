import { useMemo } from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { Card, ErrorBanner, PageHeader } from "../../../shared/ui/components";
import { formatCurrency } from "../../../shared/lib/format";
import { Link } from "react-router-dom";
import { TrendingUp, ShoppingCart, BookOpen, DollarSign } from "lucide-react";

const INITIAL_NOW = new Date().toISOString();
const INITIAL_FROM = new Date(Date.now() - 30 * 24 * 3600 * 1000).toISOString();

export default function ManagerDashboardPage() {
  const dateRange = useMemo(() => ({ from: INITIAL_FROM, now: INITIAL_NOW }), []);

  const { data, loading, error } = useLoad(
    () => api.salesReport(dateRange.from, dateRange.now),
    [dateRange.from, dateRange.now],
  );

  const chartData = useMemo(
    () => (data?.topBooks ?? []).map((b) => ({ name: b.title.length > 15 ? b.title.slice(0, 15) + "…" : b.title, sold: b.quantitySold })),
    [data],
  );

  if (loading) {
    return (
      <div className="page">
        <PageHeader title="Dashboard" subtitle="Đang tải..." />
        <div className="grid three">
          {[1, 2, 3, 4].map((i) => <div key={i} className="skeleton-card" style={{ height: 100 }} />)}
        </div>
      </div>
    );
  }

  if (error) return <ErrorBanner message={error} />;

  const growth = data?.growthPercent;

  return (
    <div className="page">
      <PageHeader title="Dashboard" subtitle="Tổng quan kinh doanh 30 ngày gần nhất" />

      <Card style={{ marginBottom: 20 }}>
        <h3 style={{ marginBottom: 8 }}>Chức năng quản lý</h3>
        <p className="muted" style={{ marginBottom: 12 }}>
          Cập nhật giá bán sách: xem danh sách, chọn sách, nhập giá mới và thời gian áp dụng.
        </p>
        <Link className="btn btn-primary" to="/manager/pricing">Cập nhật giá bán sách</Link>
      </Card>

      <div className="kpi-grid">
        <Card className="kpi-card">
          <div className="kpi-icon blue"><ShoppingCart size={20} /></div>
          <div className="kpi-body">
            <span className="kpi-value">{data?.totalOrders ?? 0}</span>
            <span className="kpi-label">Tổng đơn hàng</span>
          </div>
        </Card>
        <Card className="kpi-card">
          <div className="kpi-icon green"><DollarSign size={20} /></div>
          <div className="kpi-body">
            <span className="kpi-value">{formatCurrency(data?.totalRevenue ?? 0)}</span>
            <span className="kpi-label">Doanh thu</span>
          </div>
        </Card>
        <Card className="kpi-card">
          <div className="kpi-icon purple"><BookOpen size={20} /></div>
          <div className="kpi-body">
            <span className="kpi-value">{data?.totalBooksSold ?? 0}</span>
            <span className="kpi-label">Sách đã bán</span>
          </div>
        </Card>
        <Card className="kpi-card">
          <div className="kpi-icon orange"><TrendingUp size={20} /></div>
          <div className="kpi-body">
            <span className="kpi-value">{growth == null ? "N/A" : `${growth.toFixed(1)}%`}</span>
            <span className="kpi-label">Tăng trưởng</span>
          </div>
        </Card>
      </div>

      {chartData.length > 0 && (
        <Card>
          <h3 style={{ marginBottom: 12 }}>Top sách bán chạy</h3>
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" fontSize={12} />
              <YAxis />
              <Tooltip />
              <Bar dataKey="sold" fill="#6d5efc" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>
      )}
    </div>
  );
}
