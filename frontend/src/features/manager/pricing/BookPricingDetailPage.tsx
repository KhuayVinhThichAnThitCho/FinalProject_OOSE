import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { Card, ErrorBanner, PageHeader, Input, ConfirmDialog, EmptyState } from "../../../shared/ui/components";
import { useToast } from "../../../shared/ui/toast";
import { getErrorMessage } from "../../../shared/lib/error";
import { formatCurrency } from "../../../shared/lib/format";
import { ArrowLeft, Loader2 } from "lucide-react";

function toDatetimeLocalValue(d: Date) {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function parsePositiveIntegerPrice(raw: string): { ok: true; value: number } | { ok: false; message: string } {
  const t = raw.trim();
  if (t === "") return { ok: false, message: "Vui lòng nhập giá bán." };
  if (!/^[0-9]+$/.test(t)) {
    return {
      ok: false,
      message: "Giá bán chỉ được nhập chữ số (0–9), không chứa chữ cái hay ký tự đặc biệt.",
    };
  }
  const n = Number(t);
  if (!Number.isFinite(n) || n <= 0) {
    return { ok: false, message: "Giá bán phải lớn hơn 0." };
  }
  return { ok: true, value: n };
}

export default function BookPricingDetailPage() {
  const { bookId } = useParams();
  const navigate = useNavigate();
  const { push } = useToast();
  const id = bookId ? Number(bookId) : NaN;
  const { data, loading, error } = useLoad(() => api.managerBookDetail(id), [id]);

  const [priceInput, setPriceInput] = useState("");
  const [effectiveFromLocal, setEffectiveFromLocal] = useState(() => toDatetimeLocalValue(new Date()));
  const [priceError, setPriceError] = useState("");
  const [effectiveFromError, setEffectiveFromError] = useState("");
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [lossSaleOpen, setLossSaleOpen] = useState(false);
  const [lossSaleMsg, setLossSaleMsg] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (data) {
      setPriceInput(String(data.price));
      setPriceError("");
      setEffectiveFromError("");
    }
  }, [data]);

  if (!bookId || Number.isNaN(id)) {
    return <ErrorBanner message="Mã sách không hợp lệ." />;
  }
  if (loading) {
    return (
      <div className="page">
        <Card><Loader2 className="spin" size={20} /> Đang tải thông tin sách...</Card>
      </div>
    );
  }
  if (error) return <ErrorBanner message={error} />;
  if (!data) return <EmptyState title="Không tìm thấy" desc="Sách không tồn tại." />;

  const effectiveLabel = (() => {
    try {
      return new Date(effectiveFromLocal).toLocaleString("vi-VN");
    } catch {
      return effectiveFromLocal;
    }
  })();

  const validateFields = (): { price: number } | null => {
    setPriceError("");
    setEffectiveFromError("");
    const parsed = parsePositiveIntegerPrice(priceInput);
    if (!parsed.ok) {
      setPriceError(parsed.message);
      return null;
    }
    const d = new Date(effectiveFromLocal);
    if (Number.isNaN(d.getTime())) {
      setEffectiveFromError("Thời gian áp dụng không hợp lệ.");
      return null;
    }
    return { price: parsed.value };
  };

  const runUpdate = async (allowLossSale: boolean) => {
    const v = validateFields();
    if (!v) {
      setConfirmOpen(false);
      return;
    }
    setSubmitting(true);
    try {
      const effectiveFrom = new Date(effectiveFromLocal).toISOString();
      const res = await api.updateBookPrice(id, {
        newSalePrice: v.price,
        effectiveFrom,
        allowLossSale,
      });

      if (!res.updated && !allowLossSale) {
        setLossSaleMsg(
          res.message
            || "Giá bán hiện tại đang thấp hơn giá vốn. Bạn có chắc chắn muốn tiếp tục?",
        );
        setLossSaleOpen(true);
        return;
      }

      push(res.message || (res.updated ? "Cập nhật giá bán thành công" : "Đã xử lý"), res.updated ? "success" : "info");
      if (res.updated) navigate("/manager/pricing");
    } catch (e) {
      const msg = getErrorMessage(e, "Cập nhật giá thất bại");
      if (/giá bán|Giá bán|số nguyên|lớn hơn 0/i.test(msg)) {
        setPriceError(msg);
      } else {
        push(msg, "error");
      }
    } finally {
      setSubmitting(false);
      setConfirmOpen(false);
    }
  };

  const openConfirmIfValid = () => {
    if (!validateFields()) return;
    setConfirmOpen(true);
  };

  const parsedPreview = parsePositiveIntegerPrice(priceInput);
  const newPriceForConfirm = parsedPreview.ok ? parsedPreview.value : 0;

  return (
    <div className="page manager-pricing-detail">
      <Link to="/manager/pricing" className="back-link">
        <ArrowLeft size={16} /> Danh sách sách
      </Link>

      <PageHeader
        title="Cập nhật giá bán sách"
        subtitle="Xem thông tin chi tiết, nhập giá mới và thời gian áp dụng, rồi xác nhận hoặc hủy."
      />

      <div className="grid two">
        <Card>
          <h3>Thông tin sách</h3>
          <p><strong>{data.title}</strong></p>
          <p className="muted">Danh mục: {data.category || "—"}</p>
          <p className="muted">Tồn kho: {data.stockQuantity}</p>
          <hr style={{ margin: "16px 0", border: 0, borderTop: "1px solid #ebedf3" }} />
          <div className="pricing-detail-row">
            <span className="muted">Giá bán hiện tại</span>
            <strong>{formatCurrency(data.price)}</strong>
          </div>
          <div className="pricing-detail-row">
            <span className="muted">Giá nhập (giá vốn)</span>
            <strong>{formatCurrency(data.costPrice)}</strong>
          </div>
        </Card>

        <Card>
          <h3>Thiết lập giá mới</h3>
          <div className={`form-group ${priceError ? "has-field-error" : ""}`}>
            <label>Giá bán mới (VNĐ)</label>
            <Input
              type="text"
              inputMode="numeric"
              autoComplete="off"
              value={priceInput}
              onChange={(e) => {
                setPriceInput(e.target.value);
                if (priceError) setPriceError("");
              }}
              placeholder="Chỉ nhập số, ví dụ: 150000"
              aria-invalid={!!priceError}
            />
            {priceError ? <p className="form-field-error">{priceError}</p> : null}
          </div>
          <div className={`form-group ${effectiveFromError ? "has-field-error" : ""}`}>
            <label>Thời gian áp dụng</label>
            <Input
              type="datetime-local"
              value={effectiveFromLocal}
              onChange={(e) => {
                setEffectiveFromLocal(e.target.value);
                if (effectiveFromError) setEffectiveFromError("");
              }}
              aria-invalid={!!effectiveFromError}
            />
            {effectiveFromError ? <p className="form-field-error">{effectiveFromError}</p> : null}
            <p className="muted" style={{ marginTop: 6, fontSize: 13 }}>
              Giá mới sẽ được ghi nhận kèm mốc thời điểm áp dụng theo hệ thống.
            </p>
          </div>
          <div className="row pricing-form-actions">
            <button
              type="button"
              className="btn"
              disabled={submitting}
              onClick={() => navigate("/manager/pricing")}
            >
              Hủy cập nhật giá bán
            </button>
            <button
              type="button"
              className="btn btn-primary"
              disabled={submitting}
              onClick={openConfirmIfValid}
            >
              Xác nhận cập nhật giá bán
            </button>
          </div>
        </Card>
      </div>

      <ConfirmDialog
        open={confirmOpen}
        title="Xác nhận cập nhật giá bán?"
        body={
          <div className="confirm-pricing-body">
            <p><strong>{data.title}</strong></p>
            <p>Giá bán hiện tại: {formatCurrency(data.price)} → <strong>{formatCurrency(newPriceForConfirm)}</strong></p>
            <p>Giá nhập: {formatCurrency(data.costPrice)}</p>
            <p>Thời gian áp dụng: <strong>{effectiveLabel}</strong></p>
          </div>
        }
        onClose={() => setConfirmOpen(false)}
        onConfirm={async () => {
          await runUpdate(false);
        }}
        cancelLabel="Hủy"
        confirmLabel="Xác nhận"
      />

      <ConfirmDialog
        open={lossSaleOpen}
        title="Cảnh báo lợi nhuận"
        body={
          <div>
            <div className="pricing-loss-warning" role="alert">
              {lossSaleMsg}
            </div>
            <p className="muted" style={{ marginTop: 12 }}>
              Chọn &quot;Xác nhận&quot; để áp dụng giá bán thấp hơn giá vốn (bán lỗ), hoặc &quot;Hủy&quot; để không thay đổi.
            </p>
          </div>
        }
        onClose={() => setLossSaleOpen(false)}
        onConfirm={async () => {
          setLossSaleOpen(false);
          setSubmitting(true);
          try {
            const v = validateFields();
            if (!v) {
              push("Dữ liệu không hợp lệ. Vui lòng kiểm tra lại các trường.", "error");
              return;
            }
            const effectiveFrom = new Date(effectiveFromLocal).toISOString();
            const res = await api.updateBookPrice(id, {
              newSalePrice: v.price,
              effectiveFrom,
              allowLossSale: true,
            });
            push(res.message || "Cập nhật giá bán thành công", res.updated ? "success" : "info");
            if (res.updated) navigate("/manager/pricing");
          } catch (e) {
            push(getErrorMessage(e, "Cập nhật giá thất bại"), "error");
          } finally {
            setSubmitting(false);
          }
        }}
        cancelLabel="Hủy"
        confirmLabel="Xác nhận"
      />
    </div>
  );
}
