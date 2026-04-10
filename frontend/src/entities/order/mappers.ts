import type { OrderDetail, OrderSummary } from "../../shared/types";
import { formatCurrency, formatDateTime } from "../../shared/lib/format";

export const mapOrderSummaryVM = (o: OrderSummary) => ({
  id: o.orderId,
  dateText: formatDateTime(o.ngayDat),
  totalText: formatCurrency(o.totalAmount),
  status: o.status,
});

export const mapOrderDetailVM = (o: OrderDetail) => ({
  id: o.orderId,
  dateText: formatDateTime(o.ngayDat),
  totalText: formatCurrency(o.totalAmount),
  status: o.status,
  shipping: o.shipping,
  items: o.items.map((i) => ({
    ...i,
    unitPriceText: formatCurrency(i.unitPrice),
  })),
});
