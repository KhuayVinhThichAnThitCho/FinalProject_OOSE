export const formatCurrency = (value: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value ?? 0);

export const formatDateTime = (value: string) =>
  new Date(value).toLocaleString("vi-VN", { hour12: false });
