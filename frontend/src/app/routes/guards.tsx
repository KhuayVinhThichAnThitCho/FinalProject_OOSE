import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "../../features/auth/authStore";
import type { Role } from "../../shared/types";

export function RequireAuth() {
  const token = useAuthStore((s) => s.token);
  if (!token) return <Navigate to="/login" replace />;
  return <Outlet />;
}

export function RequireRole({ roles }: { roles: Role[] }) {
  const userRoles = useAuthStore((s) => s.roles);
  if (!roles.some((r) => userRoles.includes(r))) return <Navigate to="/unauthorized" replace />;
  return <Outlet />;
}
