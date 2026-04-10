import { createBrowserRouter, Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "../features/auth/authStore";
import { AppLayout } from "./layouts/AppLayout";
import {
  BooksPage,
  HomePage,
  LoginPage,
  ManagerBookPricePage,
  ManagerBooksPage,
  ManagerSalesReportPage,
  MyOrdersPage,
  NewCancelRequestPage,
  NewOrderPage,
  NotFoundPage,
  OrderDetailPage,
  PayOrderPage,
  ConfirmOrderPage,
  StaffCancelRequestDetailPage,
  StaffCancelRequestsPage,
  StaffOrderDetailPage,
  StaffPendingOrdersPage,
  UnauthorizedPage,
} from "./pages";
import type { Role } from "../shared/types";

function Protected({ roles }: { roles?: Role[] }) {
  const token = useAuthStore((s) => s.token);
  const userRoles = useAuthStore((s) => s.roles);
  if (!token) return <Navigate to="/login" replace />;
  if (roles && !roles.some((r) => userRoles.includes(r))) return <Navigate to="/unauthorized" replace />;
  return <Outlet />;
}

export const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  { path: "/unauthorized", element: <UnauthorizedPage /> },
  {
    element: <Protected />,
    children: [
      {
        path: "/",
        element: <AppLayout />,
        children: [
          { index: true, element: <HomePage /> },
          { path: "books", element: <Protected roles={["CUSTOMER"]} />, children: [{ index: true, element: <BooksPage /> }] },
          { path: "orders/new", element: <Protected roles={["CUSTOMER"]} />, children: [{ index: true, element: <NewOrderPage /> }] },
          { path: "orders/:orderId/confirm", element: <Protected roles={["CUSTOMER"]} />, children: [{ index: true, element: <ConfirmOrderPage /> }] },
          { path: "orders/:orderId/pay", element: <Protected roles={["CUSTOMER"]} />, children: [{ index: true, element: <PayOrderPage /> }] },
          { path: "orders/my", element: <Protected roles={["CUSTOMER"]} />, children: [{ index: true, element: <MyOrdersPage /> }] },
          { path: "orders/:orderId", element: <Protected roles={["CUSTOMER"]} />, children: [{ index: true, element: <OrderDetailPage /> }] },
          { path: "cancel-requests/new", element: <Protected roles={["CUSTOMER"]} />, children: [{ index: true, element: <NewCancelRequestPage /> }] },
          { path: "staff/orders/pending", element: <Protected roles={["STAFF", "MANAGER"]} />, children: [{ index: true, element: <StaffPendingOrdersPage /> }] },
          { path: "staff/orders/:orderId", element: <Protected roles={["STAFF", "MANAGER"]} />, children: [{ index: true, element: <StaffOrderDetailPage /> }] },
          { path: "staff/cancel-requests", element: <Protected roles={["STAFF", "MANAGER"]} />, children: [{ index: true, element: <StaffCancelRequestsPage /> }] },
          {
            path: "staff/cancel-requests/:id",
            element: <Protected roles={["STAFF", "MANAGER"]} />,
            children: [{ index: true, element: <StaffCancelRequestDetailPage /> }],
          },
          { path: "manager/books", element: <Protected roles={["MANAGER"]} />, children: [{ index: true, element: <ManagerBooksPage /> }] },
          {
            path: "manager/books/:id/price",
            element: <Protected roles={["MANAGER"]} />,
            children: [{ index: true, element: <ManagerBookPricePage /> }],
          },
          {
            path: "manager/reports/sales",
            element: <Protected roles={["MANAGER"]} />,
            children: [{ index: true, element: <ManagerSalesReportPage /> }],
          },
        ],
      },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
]);
