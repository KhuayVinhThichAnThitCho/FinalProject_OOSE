import { useMemo, useState } from "react";
import { api } from "../../../shared/api";
import { useLoad } from "../../../shared/hooks/useLoad";
import { useCartStore } from "../cart/cartStore";
import { useToast } from "../../../shared/ui/toast";
import { formatCurrency } from "../../../shared/lib/format";
import { Card, EmptyState, ErrorBanner, Input, Select } from "../../../shared/ui/components";
import { ShoppingCart, Search } from "lucide-react";

export default function CatalogPage() {
  const { data: books, loading, error } = useLoad(api.books, []);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("");
  const addItem = useCartStore((s) => s.addItem);
  const { push } = useToast();

  const categories = useMemo(() => {
    if (!books) return [];
    const set = new Set(books.map((b) => b.category ?? "General"));
    return Array.from(set).sort();
  }, [books]);

  const filtered = useMemo(() => {
    if (!books) return [];
    return books.filter((b) => {
      const matchSearch = b.title.toLowerCase().includes(search.toLowerCase());
      const matchCat = !category || (b.category ?? "General") === category;
      return matchSearch && matchCat;
    });
  }, [books, search, category]);

  if (loading) {
    return (
      <div className="catalog-skeleton">
        {Array.from({ length: 8 }).map((_, i) => (
          <div key={i} className="skeleton-card" />
        ))}
      </div>
    );
  }

  if (error) return <ErrorBanner message={error} />;

  return (
    <div className="catalog-page">
      <div className="catalog-hero">
        <h1>Khám phá sách hay</h1>
        <p>Tìm kiếm và thêm vào giỏ hàng, thanh toán khi sẵn sàng</p>
      </div>

      <div className="catalog-toolbar">
        <div className="search-box">
          <Search size={16} />
          <Input
            placeholder="Tìm kiếm theo tên sách..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <Select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="">Tất cả danh mục</option>
          {categories.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </Select>
        <span className="muted">{filtered.length} sản phẩm</span>
      </div>

      {filtered.length === 0 ? (
        <EmptyState title="Không tìm thấy sách" desc="Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm." />
      ) : (
        <div className="product-grid">
          {filtered.map((b) => (
            <Card key={b.id} className="product-card">
              <div className="product-img" />
              <div className="product-body">
                <span className="product-cat">{b.category ?? "General"}</span>
                <h3 className="product-title">{b.title}</h3>
                <p className="product-price">{formatCurrency(b.price)}</p>
                <div className="product-footer">
                  <span className={`stock-badge ${b.stockQuantity > 0 ? "in-stock" : "out-stock"}`}>
                    {b.stockQuantity > 0 ? `Còn ${b.stockQuantity}` : "Hết hàng"}
                  </span>
                  <button
                    className="btn btn-primary btn-sm"
                    disabled={b.stockQuantity <= 0}
                    onClick={() => {
                      addItem(b);
                      push(`Đã thêm "${b.title}" vào giỏ`, "success");
                    }}
                  >
                    <ShoppingCart size={14} /> Thêm
                  </button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
