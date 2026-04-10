import { useState } from "react";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { mapBookCardVM } from "../../../entities/book/mappers";
import { Card, DataTable, EmptyState, ErrorBanner, Input, PageHeader, ConfirmDialog } from "../../../shared/ui/components";
import { useToast } from "../../../shared/ui/toast";
import { getErrorMessage } from "../../../shared/lib/error";
import { Edit3, Search } from "lucide-react";

export default function PricingCenterPage() {
  const { data, loading, error, setData } = useLoad(api.managerBooks, []);
  const [search, setSearch] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [newPrice, setNewPrice] = useState(0);
  const [lossSaleOpen, setLossSaleOpen] = useState(false);
  const [lossSaleMsg, setLossSaleMsg] = useState("");
  const [pendingLossSale, setPendingLossSale] = useState<{ id: number; price: number } | null>(null);
  const { push } = useToast();

  if (loading) return <Card>Đang tải...</Card>;
  if (error) return <ErrorBanner message={error} />;
  if (!data || data.length === 0) return <EmptyState title="Không có sách" desc="Dữ liệu trống." />;

  const filtered = data.filter((b) =>
    b.title.toLowerCase().includes(search.toLowerCase()),
  );

  const doUpdate = async (bookId: number, price: number, allowLoss: boolean) => {
    try {
      const res = await api.updateBookPrice(bookId, {
        newSalePrice: price,
        effectiveFrom: new Date().toISOString(),
        allowLossSale: allowLoss,
      });

      if (!res.updated && !allowLoss) {
        setLossSaleMsg(res.message);
        setPendingLossSale({ id: bookId, price });
        setLossSaleOpen(true);
        return;
      }

      push(res.message, res.updated ? "success" : "info");
      setEditingId(null);
      const refreshed = await api.managerBooks();
      setData(refreshed);
    } catch (e) {
      push(getErrorMessage(e, "Cập nhật giá thất bại"), "error");
    }
  };

  return (
    <div className="page">
      <PageHeader
        title="Quản lý giá bán"
        subtitle="Cập nhật giá bán sách theo workflow tập trung"
        actions={
          <div className="search-box">
            <Search size={16} />
            <Input placeholder="Tìm sách..." value={search} onChange={(e) => setSearch(e.target.value)} />
          </div>
        }
      />

      <DataTable
        headers={["Sách", "Danh mục", "Giá bán", "Giá vốn", "Tồn kho", ""]}
        rows={filtered.map((b) => {
          const vm = mapBookCardVM(b);
          const isEditing = editingId === vm.id;
          return [
            vm.title,
            vm.category,
            isEditing ? (
              <Input
                type="number"
                min={1}
                value={newPrice}
                onChange={(e) => setNewPrice(Number(e.target.value))}
                style={{ width: 120 }}
              />
            ) : (
              vm.salePriceText
            ),
            vm.costPriceText,
            vm.stock,
            isEditing ? (
              <div className="row">
                <button className="btn btn-primary btn-sm" onClick={() => doUpdate(vm.id, newPrice, false)}>
                  Lưu
                </button>
                <button className="btn btn-sm" onClick={() => setEditingId(null)}>
                  Hủy
                </button>
              </div>
            ) : (
              <button
                className="btn btn-sm"
                onClick={() => { setEditingId(vm.id); setNewPrice(vm.salePrice); }}
              >
                <Edit3 size={14} /> Sửa giá
              </button>
            ),
          ];
        })}
      />

      <ConfirmDialog
        open={lossSaleOpen}
        title="Xác nhận bán lỗ?"
        body={
          <div>
            <p>{lossSaleMsg}</p>
            <p className="muted" style={{ marginTop: 8 }}>Bạn có chắc muốn đặt giá dưới giá vốn?</p>
          </div>
        }
        onClose={() => { setLossSaleOpen(false); setPendingLossSale(null); }}
        onConfirm={async () => {
          if (pendingLossSale) {
            setLossSaleOpen(false);
            await doUpdate(pendingLossSale.id, pendingLossSale.price, true);
            setPendingLossSale(null);
          }
        }}
      />
    </div>
  );
}
