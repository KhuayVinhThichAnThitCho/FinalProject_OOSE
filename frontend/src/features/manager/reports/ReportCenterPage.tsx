import { useMemo, useState, useEffect, useRef } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { api } from "../../../shared/api";
import { getErrorMessage } from "../../../shared/lib/error";
import { formatCurrency } from "../../../shared/lib/format";
import { Card, ErrorBanner, Input, PageHeader, Select } from "../../../shared/ui/components";
import { useToast } from "../../../shared/ui/toast";
import type { SalesReportData } from "../../../shared/types";
import { Download, BarChart3, PieChart as PieChartIcon, Search, TrendingDown, TrendingUp } from "lucide-react";

const CHART_COLORS = ["#6d5efc", "#22c55e", "#f97316", "#3b82f6", "#a855f7", "#ec4899", "#14b8a6", "#eab308"];

/** Đầu ngày UTC (khớp backend Instant). */
function rangeFromIso(dateStr: string) {
  return dateStr ? new Date(`${dateStr}T00:00:00.000Z`).toISOString() : "";
}
/** Cuối ngày UTC — tránh loại bỏ toàn bộ đơn trong ngày “đến ngày”. */
function rangeToIso(dateStr: string) {
  return dateStr ? new Date(`${dateStr}T23:59:59.999Z`).toISOString() : "";
}

function toDateInput(iso: string) {
  return iso.slice(0, 10);
}

export default function ReportCenterPage() {
  const [fromDate, setFromDate] = useState(toDateInput(new Date(Date.now() - 30 * 24 * 3600 * 1000).toISOString()));
  const [toDate, setToDate] = useState(toDateInput(new Date().toISOString()));
  const [category, setCategory] = useState("");
  const [orderStatus, setOrderStatus] = useState("");
  const [report, setReport] = useState<SalesReportData | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { push } = useToast();
  const initialLoadDone = useRef(false);

  const chartData = useMemo(
    () => (report?.topBooks ?? []).map((b) => ({ name: b.title.length > 15 ? b.title.slice(0, 15) + "…" : b.title, sold: b.quantitySold })),
    [report],
  );

  /** Gộp theo danh mục từ danh sách top sách (cùng nguồn dữ liệu KPI). */
  const categoryPieData = useMemo(() => {
    if (!report?.topBooks?.length) return [];
    const m = new Map<string, number>();
    for (const b of report.topBooks) {
      const cat = b.category?.trim() ? b.category.trim() : "Không phân loại";
      m.set(cat, (m.get(cat) ?? 0) + b.quantitySold);
    }
    return [...m.entries()]
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value);
  }, [report]);

  const revenueCompareData = useMemo(() => {
    if (!report) return [];
    return [
      { name: "Kỳ này", doanhThu: report.totalRevenue },
      { name: "Kỳ trước", doanhThu: report.prevRevenue },
    ];
  }, [report]);

  const fetchReport = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.salesReport(
        rangeFromIso(fromDate),
        rangeToIso(toDate),
        category.trim() || undefined,
        orderStatus || undefined,
      );
      setReport(data);
    } catch (e) {
      setError(getErrorMessage(e, "Tải báo cáo thất bại"));
      setReport(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (initialLoadDone.current) return;
    initialLoadDone.current = true;
    void fetchReport();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- chỉ tải mặc định một lần khi vào trang
  }, []);

  const doExport = async (format: "xlsx" | "pdf") => {
    try {
      const blob = await api.exportReport(
        format,
        rangeFromIso(fromDate),
        rangeToIso(toDate),
        category.trim() || undefined,
        orderStatus || undefined,
      );
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `bao-cao-ban-hang.${format}`;
      a.click();
      URL.revokeObjectURL(url);
      push(`Đã tải file ${format.toUpperCase()}`, "success");
    } catch (e) {
      push(getErrorMessage(e, `Xuất ${format.toUpperCase()} thất bại`), "error");
    }
  };

  const growthText =
    report?.growthPercent == null
      ? "—"
      : `${report.growthPercent >= 0 ? "+" : ""}${report.growthPercent.toFixed(1)}%`;

  const noSalesData = Boolean(report && report.totalOrders === 0);

  return (
    <div className="page">
      <PageHeader
        title="Xem báo cáo bán hàng"
        subtitle="Chọn khoảng thời gian và bộ lọc (danh mục, trạng thái đơn). Hệ thống so sánh doanh thu với kỳ liền trước cùng độ dài."
      />

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
            <label>Danh mục sách (tùy chọn)</label>
            <Input placeholder="Để trống = tất cả" value={category} onChange={(e) => setCategory(e.target.value)} />
          </div>
          <div className="form-group">
            <label>Trạng thái đơn (tùy chọn)</label>
            <Select value={orderStatus} onChange={(e) => setOrderStatus(e.target.value)}>
              <option value="">DELIVERED (mặc định)</option>
              <option value="PAID">PAID</option>
              <option value="SHIPPING">SHIPPING</option>
              <option value="DELIVERED">DELIVERED</option>
              <option value="PROCESSING">PROCESSING</option>
              <option value="CANCELLED">CANCELLED</option>
            </Select>
          </div>
        </div>
        <div className="row" style={{ marginTop: 12, flexWrap: "wrap", gap: 10 }}>
          <button type="button" className="btn btn-primary" disabled={loading} onClick={() => void fetchReport()}>
            <Search size={16} /> {loading ? "Đang tải..." : "Xem báo cáo"}
          </button>
          <button type="button" className="btn" disabled={!report || noSalesData} onClick={() => void doExport("xlsx")}>
            <Download size={16} /> Xuất báo cáo (Excel)
          </button>
          <button type="button" className="btn" disabled={!report || noSalesData} onClick={() => void doExport("pdf")}>
            <Download size={16} /> Xuất báo cáo (PDF)
          </button>
        </div>
        {error && <ErrorBanner message={error} />}
      </Card>

      {report && (
        <>
          {noSalesData && (
            <Card className="report-empty-flow">
              <h3>Không có dữ liệu bán hàng</h3>
              <p style={{ margin: "0 0 10px", lineHeight: 1.5 }}>
                Hệ thống đã kiểm tra và không tìm thấy dữ liệu bán hàng khớp với khoảng thời gian hoặc bộ lọc đã chọn.
              </p>
              <p className="muted" style={{ margin: "0 0 10px", lineHeight: 1.5 }}>
                <strong>Thông báo:</strong>{" "}
                {report.message ?? "Không có dữ liệu bán hàng phù hợp với tiêu chí tìm kiếm"}
              </p>
              <p className="muted" style={{ margin: 0, lineHeight: 1.5 }}>
                Quản lý có thể điều chỉnh lại khoảng thời gian hoặc bộ lọc, bấm «Xem báo cáo», hoặc kết thúc nếu không cần xem tiếp.
              </p>
            </Card>
          )}

          <div className={`kpi-grid report-kpi-grid${noSalesData ? " is-muted" : ""}`}>
            <Card className="kpi-card">
              <div className="kpi-icon blue"><BarChart3 size={20} /></div>
              <div className="kpi-body">
                <span className="kpi-value">{report.totalOrders}</span>
                <span className="kpi-label">Tổng số đơn hàng</span>
              </div>
            </Card>
            <Card className="kpi-card">
              <div className="kpi-icon green"><BarChart3 size={20} /></div>
              <div className="kpi-body">
                <span className="kpi-value">{formatCurrency(report.totalRevenue)}</span>
                <span className="kpi-label">Tổng doanh thu (kỳ này)</span>
                <span className="kpi-sub muted">Kỳ trước: {formatCurrency(report.prevRevenue)}</span>
              </div>
            </Card>
            <Card className="kpi-card">
              <div className={`kpi-icon ${report.growthPercent != null && report.growthPercent < 0 ? "orange" : "purple"}`}>
                {report.growthPercent != null && report.growthPercent < 0 ? <TrendingDown size={20} /> : <TrendingUp size={20} />}
              </div>
              <div className="kpi-body">
                <span className="kpi-value">{growthText}</span>
                <span className="kpi-label">Tăng trưởng so với kỳ trước</span>
              </div>
            </Card>
            <Card className="kpi-card">
              <div className="kpi-icon purple"><BarChart3 size={20} /></div>
              <div className="kpi-body">
                <span className="kpi-value">{report.totalBooksSold}</span>
                <span className="kpi-label">Số lượng sách đã bán (cuốn)</span>
              </div>
            </Card>
          </div>

          {noSalesData ? (
            <Card>
              <h3 style={{ marginTop: 0, marginBottom: 8, fontSize: "1rem" }}>Biểu đồ</h3>
              <div className="report-charts-empty">
                Không có dữ liệu để hiển thị biểu đồ (top sách, so sánh doanh thu, phân bổ danh mục) trong kỳ và bộ lọc hiện tại.
              </div>
            </Card>
          ) : (
            <div className="report-charts-grid">
              <Card className="report-chart-full">
                <h3 style={{ marginBottom: 12 }}>Top sách bán chạy (cột)</h3>
                {chartData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={chartData} margin={{ bottom: 8, left: 4, right: 8 }}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" fontSize={11} interval={0} angle={-18} textAnchor="end" height={72} />
                      <YAxis allowDecimals={false} />
                      <Tooltip
                        formatter={(value) => {
                          const v = typeof value === "number" ? value : Number(value);
                          return [Number.isFinite(v) ? `${v} cuốn` : "—", "Đã bán"];
                        }}
                      />
                      <Bar dataKey="sold" fill="#6d5efc" radius={[6, 6, 0, 0]} name="Đã bán" />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <p className="muted">Không có dữ liệu xếp hạng theo sách trong kỳ (hoặc đơn không có dòng chi tiết).</p>
                )}
              </Card>

              <Card>
                <h3 style={{ marginBottom: 12, display: "flex", alignItems: "center", gap: 8 }}>
                  <BarChart3 size={20} className="muted" style={{ flexShrink: 0 }} />
                  So sánh doanh thu
                </h3>
                <p className="muted" style={{ margin: "0 0 12px", fontSize: 13 }}>
                  Hai cột theo cùng bộ lọc; kỳ trước là đoạn thời gian liền trước, độ dài bằng kỳ này.
                </p>
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={revenueCompareData} margin={{ left: 8, right: 8 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" fontSize={12} />
                    <YAxis tickFormatter={(v) => (v >= 1_000_000 ? `${(v / 1_000_000).toFixed(1)}M` : `${Math.round(v / 1000)}k`)} />
                    <Tooltip
                      formatter={(value) => {
                        const v = typeof value === "number" ? value : Number(value);
                        return Number.isFinite(v) ? formatCurrency(v) : "—";
                      }}
                    />
                    <Bar dataKey="doanhThu" radius={[6, 6, 0, 0]} name="Doanh thu">
                      {revenueCompareData.map((_, i) => (
                        <Cell key={i} fill={i === 0 ? "#6d5efc" : "#94a3b8"} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </Card>

              <Card>
                <h3 style={{ marginBottom: 12, display: "flex", alignItems: "center", gap: 8 }}>
                  <PieChartIcon size={20} className="muted" style={{ flexShrink: 0 }} />
                  Tỷ trọng SL bán theo danh mục
                </h3>
                <p className="muted" style={{ margin: "0 0 12px", fontSize: 13 }}>
                  Phân bổ số lượng trong nhóm top sách (theo danh mục từng cuốn).
                </p>
                {categoryPieData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={280}>
                    <PieChart>
                      <Pie
                        data={categoryPieData}
                        dataKey="value"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        innerRadius={56}
                        outerRadius={96}
                        paddingAngle={2}
                        label={
                          categoryPieData.length > 6
                            ? false
                            : ({ name, percent }) => {
                                const p = typeof percent === "number" ? percent : 0;
                                return `${String(name).slice(0, 12)}${String(name).length > 12 ? "…" : ""} ${(p * 100).toFixed(0)}%`;
                              }
                        }
                        labelLine={categoryPieData.length <= 6}
                      >
                        {categoryPieData.map((_, i) => (
                          <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value) => {
                          const v = typeof value === "number" ? value : Number(value);
                          return Number.isFinite(v) ? `${v} cuốn` : "—";
                        }}
                      />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                ) : (
                  <p className="muted">Không đủ dữ liệu để vẽ biểu đồ theo danh mục.</p>
                )}
              </Card>
            </div>
          )}
        </>
      )}
    </div>
  );
}
