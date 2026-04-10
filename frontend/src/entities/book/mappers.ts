import type { Book } from "../../shared/types";
import { formatCurrency } from "../../shared/lib/format";

export const mapBookCardVM = (b: Book) => ({
  id: b.id,
  title: b.title,
  category: b.category || "General",
  stock: b.stockQuantity,
  salePriceText: formatCurrency(b.price),
  costPriceText: formatCurrency(b.costPrice),
  salePrice: b.price,
});
