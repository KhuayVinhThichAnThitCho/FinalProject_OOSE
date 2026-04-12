/* eslint-disable react-refresh/only-export-components */
import { Suspense, lazy } from "react";
import type { ReactNode } from "react";
import { createBrowserRouter, Navigate } from "react-router-dom";
import { useAuthStore } from "../../features/auth/authStore";
import { CustomerShell } from "../shell/CustomerShell";
import { AdminShell } from "../shell/ProductShell";
import { NotFoundPage, UnauthorizedPage } from "./CommonPages";
import { RequireAuth, RequireRole } from "./guards";

const LoginPage = lazy(() => import("../../features/auth/LoginPage"));
const CatalogPage = lazy(() => import("../../features/customer/catalog/CatalogPage"));
const CartPage = lazy(() => import("../../features/customer/cart/CartPage"));
const CheckoutPage = lazy(() => import("../../features/customer/checkout/CheckoutPage"));
const OrdersPage = lazy(() => import("../../features/customer/orders/OrdersPage"));
const OrderDetailPage = lazy(() => import("../../features/customer/orders/OrderDetailPage"));

const OrderQueuePage = lazy(() => import("../../features/staff/orders/OrderQueuePage"));
const OrderWorkspaceDetailPage = lazy(() => import("../../features/staff/orders/OrderWorkspaceDetailPage"));
const CancelQueuePage = lazy(() => import("../../features/staff/cancellations/CancelQueuePage"));
const CancelDetailPage = lazy(() => import("../../features/staff/cancellations/CancelDetailPage"));

const ManagerDashboardPage = lazy(() => import("../../features/manager/dashboard/ManagerDashboardPage"));
const PricingCenterPage = lazy(() => import("../../features/manager/pricing/PricingCenterPage"));
const BookPricingDetailPage = lazy(() => import("../../features/manager/pricing/BookPricingDetailPage"));
const ReportCenterPage = lazy(() => import("../../features/manager/reports/ReportCenterPage"));

function RoleHome() {
  const roles = useAuthStore((s) => s.roles);
  if (roles.includes("MANAGER")) return <Navigate to="/manager/dashboard" replace />;
  if (roles.includes("STAFF")) return <Navigate to="/staff/orders" replace />;
  return <Navigate to="/customer/catalog" replace />;
}

const S = (node: ReactNode) => (
  <Suspense fallback={<div className="skeleton-card" style={{ margin: 24 }} />}>{node}</Suspense>
);

export const router = createBrowserRouter([
  { path: "/login", element: S(<LoginPage />) },
  { path: "/unauthorized", element: <UnauthorizedPage /> },
  {
    element: <RequireAuth />,
    children: [
      { index: true, path: "/", element: <RoleHome /> },

      {
        path: "/customer",
        element: <RequireRole roles={["CUSTOMER"]} />,
        children: [
          {
            element: <CustomerShell />,
            children: [
              { path: "catalog", element: S(<CatalogPage />) },
              { path: "cart", element: S(<CartPage />) },
              { path: "checkout", element: S(<CheckoutPage />) },
              { path: "orders", element: S(<OrdersPage />) },
              { path: "orders/:id", element: S(<OrderDetailPage />) },
              { index: true, element: <Navigate to="catalog" replace /> },
            ],
          },
        ],
      },

      {
        path: "/staff",
        element: <RequireRole roles={["STAFF", "MANAGER"]} />,
        children: [
          {
            element: <AdminShell />,
            children: [
              { path: "orders", element: S(<OrderQueuePage />) },
              { path: "orders/:id", element: S(<OrderWorkspaceDetailPage />) },
              { path: "cancellations", element: S(<CancelQueuePage />) },
              { path: "cancellations/:id", element: S(<CancelDetailPage />) },
              { index: true, element: <Navigate to="orders" replace /> },
            ],
          },
        ],
      },

      {
        path: "/manager",
        element: <RequireRole roles={["MANAGER"]} />,
        children: [
          {
            element: <AdminShell />,
            children: [
              { path: "dashboard", element: S(<ManagerDashboardPage />) },
              { path: "pricing/:bookId", element: S(<BookPricingDetailPage />) },
              { path: "pricing", element: S(<PricingCenterPage />) },
              { path: "reports", element: S(<ReportCenterPage />) },
              { index: true, element: <Navigate to="dashboard" replace /> },
            ],
          },
        ],
      },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
]);
