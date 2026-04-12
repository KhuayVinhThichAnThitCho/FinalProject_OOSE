import { apiClient } from "./http/apiClient";
import type {
  Book,
  CancelRequest,
  CancelRequestDetail,
  CheckoutResponse,
  CreateOrderResponse,
  LoginResponse,
  OrderDetail,
  OrderListResponse,
  OrderSummary,
  PricingResult,
  SalesReportData,
  ReportOptions,
  IdLike,
} from "./types";

export const api = {
  login: async (username: string, password: string) => {
    const { data } = await apiClient.post<LoginResponse>("/api/auth/login", { username, password });
    return data;
  },
  books: async () => {
    const { data } = await apiClient.get<Book[]>("/api/books");
    return data;
  },
  createOrder: async (customerId: number) => {
    const { data } = await apiClient.post<CreateOrderResponse>("/api/orders", { customerId });
    return data;
  },
  confirmOrder: async (orderId: IdLike, payload: unknown) => {
    const { data } = await apiClient.post<CreateOrderResponse>(`/api/orders/${orderId}/confirm`, payload);
    return data;
  },
  checkout: async (orderId: IdLike, paymentMethodCode: string) => {
    const { data } = await apiClient.post<CheckoutResponse>(`/api/orders/${orderId}/checkout`, {
      paymentMethodCode,
    });
    return data;
  },
  myOrders: async () => {
    const { data } = await apiClient.get<OrderListResponse>("/api/orders/my");
    return data;
  },
  myOrderDetail: async (orderId: IdLike, customerId: number) => {
    const { data } = await apiClient.get<OrderDetail>(`/api/orders/${orderId}`, {
      params: { customerId },
    });
    return data;
  },
  createCancelRequest: async (orderId: IdLike, reason: string) => {
    const { data } = await apiClient.post<number>("/api/cancel-requests", { orderId, reason });
    return data;
  },
  staffPendingOrders: async () => {
    const { data } = await apiClient.get<OrderSummary[]>("/api/staff/orders/pending");
    return data;
  },
  staffShippingOrders: async () => {
    const { data } = await apiClient.get<OrderSummary[]>("/api/staff/orders/shipping");
    return data;
  },
  staffOrderDetail: async (orderId: IdLike) => {
    const { data } = await apiClient.get<OrderDetail>(`/api/staff/orders/${orderId}`);
    return data;
  },
  staffConfirmOrder: async (orderId: IdLike) => {
    const { data } = await apiClient.post<string>(`/api/staff/orders/${orderId}/confirm`);
    return data;
  },
  staffMarkDelivered: async (orderId: IdLike) => {
    const { data } = await apiClient.post<string>(`/api/staff/orders/${orderId}/deliver`);
    return data;
  },
  staffCancelOrderConfirmation: async (orderId: IdLike) => {
    const { data } = await apiClient.post<string>(`/api/staff/orders/${orderId}/cancel-processing`);
    return data;
  },
  staffCancelRequests: async (status?: string) => {
    const { data } = await apiClient.get<CancelRequest[]>("/api/cancel-requests/staff", {
      params: status ? { status } : {},
    });
    return data;
  },
  staffCancelDetail: async (id: number) => {
    const { data } = await apiClient.get<CancelRequestDetail>(`/api/cancel-requests/staff/${id}`);
    return data;
  },
  approveCancel: async (id: number) => {
    const { data } = await apiClient.post<string>(`/api/cancel-requests/${id}/approve`);
    return data;
  },
  rejectCancel: async (id: number) => {
    const { data } = await apiClient.post<string>(`/api/cancel-requests/${id}/reject`);
    return data;
  },
  managerBooks: async () => {
    const { data } = await apiClient.get<Book[]>("/api/manager/books");
    return data;
  },
  managerBookDetail: async (id: number) => {
    const { data } = await apiClient.get<Book>(`/api/manager/books/${id}`);
    return data;
  },
  updateBookPrice: async (id: number, payload: { newSalePrice: number; effectiveFrom: string; allowLossSale: boolean }) => {
    const { data } = await apiClient.put<PricingResult>(`/api/manager/books/${id}/price`, payload);
    return data;
  },
  reportOptions: async () => {
    const { data } = await apiClient.get<ReportOptions>("/api/reports/sales/options");
    return data;
  },
  salesReport: async (fromDate: string, toDate: string, category?: string, orderStatus?: string) => {
    const { data } = await apiClient.get<SalesReportData>("/api/reports/sales", {
      params: { fromDate, toDate, category, orderStatus },
    });
    return data;
  },
  mockAuthorize: async (orderId: IdLike, result: string) => {
    const { data } = await apiClient.post<{ orderId: string; status: string }>(
      "/api/payment/mock/authorize",
      { orderId, result },
    );
    return data;
  },
  exportReport: async (
    fileFormat: "xlsx" | "pdf",
    fromDate: string,
    toDate: string,
    category?: string,
    orderStatus?: string,
  ) => {
    const response = await apiClient.get(`/api/reports/sales/export`, {
      params: { fromDate, toDate, fileFormat, category, orderStatus },
      responseType: "blob",
    });
    return response.data as Blob;
  },
};
