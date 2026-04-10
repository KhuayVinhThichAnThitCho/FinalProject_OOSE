export type Role = "CUSTOMER" | "STAFF" | "MANAGER";

export type LoginResponse = {
  token: string;
  tokenType: string;
  username: string;
  roles: Role[];
};

export type Book = {
  id: number;
  title: string;
  price: number;
  costPrice: number;
  category?: string;
  stockQuantity: number;
};

export type OrderItem = {
  bookId: number;
  title: string;
  quantity: number;
  unitPrice: number;
};

export type ShippingInfo = {
  address: string;
  receiverName: string;
  receiverPhone: string;
  shippingStatus?: string;
};

export type OrderSummary = {
  orderId: number;
  ngayDat: string;
  totalAmount: number;
  status: string;
};

export type OrderListResponse = {
  orders: OrderSummary[];
  message: string | null;
};

export type OrderDetail = {
  orderId: number;
  ngayDat: string;
  totalAmount: number;
  status: string;
  shipping: ShippingInfo | null;
  items: OrderItem[];
};

export type CreateOrderResponse = {
  orderId: number;
  orderedAt: string;
  shippingFee: number;
  totalAmount: number;
  status: string;
  shipping: ShippingInfo | null;
  items: OrderItem[];
};

export type CheckoutResponse = {
  orderId: number;
  status: string;
  message: string;
};

export type CancelRequest = {
  id: number;
  reason: string;
  requestedAt: string;
  status: string;
  order: { id: number; status: string };
};

export type CancelRequestDetail = {
  cancelRequestId: number;
  status: string;
  reason: string;
  requestedAt: string;
  orderDetail: OrderDetail;
};

export type PricingResult = {
  updated: boolean;
  message: string;
  costPrice: number;
  oldSalePrice: number;
  newSalePrice: number;
};

export type ReportBookAgg = {
  bookId: number;
  title: string;
  category: string | null;
  quantitySold: number;
};

export type SalesReportData = {
  from: string;
  to: string;
  totalOrders: number;
  totalRevenue: number;
  prevRevenue: number;
  growthPercent: number | null;
  totalBooksSold: number;
  topBooks: ReportBookAgg[];
  message: string | null;
};
