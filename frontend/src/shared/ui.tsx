import { clsx } from "clsx";
import type { PropsWithChildren, ReactNode } from "react";
import { createContext, useContext, useMemo, useState } from "react";

export const formatCurrency = (value: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value ?? 0);

export const formatDateTime = (value: string) =>
  new Date(value).toLocaleString("vi-VN", { hour12: false });

export const statusTone = (status: string) => {
  const s = status.toUpperCase();
  if (["PAID", "DELIVERED"].includes(s)) return "ok";
  if (["PENDING_PAYMENT", "PROCESSING", "SHIPPING", "PENDING"].includes(s)) return "warn";
  if (["CANCELED", "FAILED", "REJECTED"].includes(s)) return "danger";
  return "neutral";
};

export function Button(props: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  return <button {...props} className={clsx("btn", props.className)} />;
}

export function Card({ children, className }: PropsWithChildren<{ className?: string }>) {
  return <section className={clsx("card", className)}>{children}</section>;
}

export function Input(props: React.InputHTMLAttributes<HTMLInputElement>) {
  return <input {...props} className={clsx("input", props.className)} />;
}

export function Select(props: React.SelectHTMLAttributes<HTMLSelectElement>) {
  return <select {...props} className={clsx("select", props.className)} />;
}

export function StatusBadge({ status }: { status: string }) {
  return (
    <span className={`badge badge-${statusTone(status)}`}>
      {status.replaceAll("_", " ")}
    </span>
  );
}

export function EmptyState({ title, desc }: { title: string; desc: string }) {
  return (
    <Card className="empty">
      <h3>{title}</h3>
      <p>{desc}</p>
    </Card>
  );
}

type ToastLevel = "success" | "error" | "info";
type Toast = { id: number; text: string; level: ToastLevel };

const ToastContext = createContext<{ push: (text: string, level?: ToastLevel) => void } | null>(null);

export function ToastProvider({ children }: PropsWithChildren) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const api = useMemo(
    () => ({
      push: (text: string, level: ToastLevel = "info") => {
        const id = Date.now();
        setToasts((prev) => [...prev, { id, text, level }]);
        window.setTimeout(() => {
          setToasts((prev) => prev.filter((t) => t.id !== id));
        }, 2600);
      },
    }),
    []
  );
  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className="toast-wrap">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.level}`}>
            {t.text}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export const useToast = () => {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error("useToast must be used in ToastProvider");
  return ctx;
};

export function ConfirmDialog({
  open,
  title,
  body,
  onConfirm,
  onClose,
}: {
  open: boolean;
  title: string;
  body?: ReactNode;
  onConfirm: () => void;
  onClose: () => void;
}) {
  if (!open) return null;
  return (
    <div className="dialog-backdrop">
      <div className="dialog">
        <h3>{title}</h3>
        {body ? <div className="dialog-body">{body}</div> : null}
        <div className="dialog-actions">
          <Button onClick={onClose}>Cancel</Button>
          <Button className="btn-primary" onClick={onConfirm}>
            Confirm
          </Button>
        </div>
      </div>
    </div>
  );
}
