import { useMemo, useState } from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { api } from "../../../shared/api";
import { getErrorMessage } from "../../../shared/lib/error";
import { formatCurrency } from "../../../shared/lib/format";
import { Card, ErrorBanner, Input, PageHeader, Select } from "../../../shared/ui/components";
import { useToast } from "../../../shared/ui/toast";
import type { SalesReportData } from "../../../shared/types";
import { Download, BarChart3, Search } from "lucide-react";

function toDateInput(iso: string) {
  return iso.slice(0, 10);
}
function fromDateInput(dateStr: string) {
  return dateStr ? new Date(dateStr + "T00:00:00Z").toISOString() : "";
}

export default function ReportCenterPage() {
  const [fromDate, setFromDate] = useState(toDateInput(new Date(Date.now() - 30 * 24 * 3600 * 1000).toISOString()));
  const [toDate, setToDate] = useState(toDateInput(new Date().toISOString()));
  const [category, setCategory] = useState("");
  const [status, setStatus] = useState("");
  const [report, setReport] = useState<SalesReportData | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { push } = useToast();

  const chartData = useMemo(
    () => (report?.topBooks ?? []).map((b) => ({ name: b.title.length > 15 ? b.title.slice(0, 15) + "…" : b.title, sold: b.quantitySold })),
    [report],
  );

  const fetchReport = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.salesReport(
        fromDateInput(fromDate),
        fromDateInput(toDate),
        category || undefined,
        status || undefined,
      );
      setReport(data);
    } catch (e) {
      setError(getErrorMessage(e, "Tải báo cáo thất bại"));
    } finally {
      setLoading(false);
    }
  };

  const doExport = async (format: "xlsx" | "pdf") => {
    try {
      const blob = await api.exportReport(format, fromDateInput(fromDate), fromDateInput(toDate), category || undefined, status || undefined);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `sales-report.${format}`;
      a.click();
      URL.revokeObjectURL(url);
      push(`Xuất ${format.toUpperCase()} thành công`, "success");
    } catch (e) {
      push(getErrorMessage(e, `Xuất ${format.toUpperCase()} thất bại`), "error");
    }
  };

  return (
    <div className="page">
      <PageHeader title="Báo cáo doanh thu" subtitle="Phân tích kinh doanh theo khoảng thời gian" />

      <Card>
        <div className="report-filters">
          <div className="form-group">
            <label>Từ ngày</label>
            <Input type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} />
          </div>
          <div className="form-group">
            <label>Đến ngày</label>
            <Input type="date" value={toDate} onChange={(e) => setToDate(e.target.value)} />
          </div>
          <div className="form-group">
            <label>Danh mục</label>
            <Input placeholder="Tất cả" value={category} onChange={(e) => setCategory(e.target.value)} />
          </div>
          <div className="form-group">
            <label>Trạng thái đơn</label>
            <Select value={status} onChange={(e) => setStatus(e.target.value)}>
              <option value="">Mặc định (PAID)</option>
              <option value="PAID">PAID</option>
              <option value="SHIPPING">SHIPPING</option>
              <option value="DELIVERED">DELIVERED</option>
            </Select>
          </div>
        </div>
        <div className="row" style={{ marginTop: 12 }}>
          <button className="btn btn-primary" disabled={loading} onClick={fetchReport}>
            <Search size={16} /> {loading ? "Đang tải..." : "Xem báo cáo"}
          </button>
          <button className="btn" onClick={() => doExport("xlsx")}>
            <Download size={16} /> XLSX
          </button>
          <button className="btn" onClick={() => doExport("pdf")}>
            <Download size={16} /> PDF
          </button>
        </div>
        {error && <ErrorBanner message={error} />}
      </Card>

      {report && (
        <>
          <div className="kpi-grid">
            <Card className="kpi-card">
              <div className="kpi-icon blue"><BarChart3 size={20} /></div>
              <div className="kpi-body">
                <span className="kpi-value">{report.totalOrders}</span>
                <span className="kpi-label">Tổng đơn</span>
              </div>
            </Card>
            <Card className="kpi-card">
              <div className="kpi-icon green"><BarChart3 size={20} /></div>
              <div className="kpi-body">
                <span className="kpi-value">{formatCurrency(report.totalRevenue)}</span>
                <span className="kpi-label">Doanh thu</span>
              </div>
            </Card>
            <Card className="kpi-card">
              <div className="kpi-icon purple"><BarChart3 size={20} /></div>
              <div className="kpi-body">
                <span className="kpi-value">{report.totalBooksSold}</span>
                <span className="kpi-label">Sách bán ra</span>
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
        </>
      )}
    </div>
  );
}
