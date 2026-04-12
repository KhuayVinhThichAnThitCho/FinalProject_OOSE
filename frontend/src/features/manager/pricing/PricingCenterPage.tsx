import { useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { mapBookCardVM } from "../../../entities/book/mappers";
import { Card, DataTable, EmptyState, ErrorBanner, Input, PageHeader } from "../../../shared/ui/components";
import { Search, ChevronRight } from "lucide-react";

export default function PricingCenterPage() {
  const { data, loading, error } = useLoad(api.managerBooks, []);
  const [search, setSearch] = useState("");

  if (loading) return <Card>Đang tải danh sách sách...</Card>;
  if (error) return <ErrorBanner message={error} />;
  if (!data || data.length === 0) return <EmptyState title="Không có sách" desc="Hệ thống chưa có dữ liệu sách." />;

  const filtered = data.filter((b) => b.title.toLowerCase().includes(search.toLowerCase()));

  return (
    <div className="page">
      <PageHeader
        title="Cập nhật giá bán sách"
        subtitle="Bước 1–2: Xem danh sách sách trong hệ thống. Chọn một cuốn để xem chi tiết và cập nhật giá."
        actions={
          <div className="search-box">
            <Search size={16} />
            <Input placeholder="Tìm theo tên sách..." value={search} onChange={(e) => setSearch(e.target.value)} />
          </div>
        }
      />

      <DataTable
        headers={["Tên sách", "Danh mục", "Giá bán hiện tại", "Giá nhập", "Tồn kho", "Thao tác"]}
        rows={filtered.map((b) => {
          const vm = mapBookCardVM(b);
          return [
            vm.title,
            vm.category,
            vm.salePriceText,
            vm.costPriceText,
            vm.stock,
            <Link key={vm.id} className="btn btn-sm btn-primary" to={`/manager/pricing/${vm.id}`}>
              Chọn <ChevronRight size={14} />
            </Link>,
          ];
        })}
      />
    </div>
  );
}
