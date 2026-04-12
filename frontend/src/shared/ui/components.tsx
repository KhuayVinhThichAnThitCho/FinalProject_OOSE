import { clsx } from "clsx";
import type { PropsWithChildren, ReactNode } from "react";

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

export function PageHeader({ title, subtitle, actions }: { title: string; subtitle?: string; actions?: ReactNode }) {
  return (
    <div className="page-header">
      <div>
        <h2>{title}</h2>
        {subtitle ? <p className="muted">{subtitle}</p> : null}
      </div>
      {actions ? <div className="row">{actions}</div> : null}
    </div>
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

export function ErrorBanner({ message }: { message: string }) {
  return <div className="error-banner">{message}</div>;
}

const statusTone = (status: string) => {
  const s = status.toUpperCase();
  if (["PAID", "DELIVERED", "APPROVED"].includes(s)) return "ok";
  if (["PENDING_PAYMENT", "PROCESSING", "SHIPPING", "PENDING"].includes(s)) return "warn";
  if (["CANCELED", "FAILED", "REJECTED"].includes(s)) return "danger";
  return "neutral";
};

export function StatusBadge({ status }: { status: string }) {
  return <span className={`badge badge-${statusTone(status)}`}>{status.replaceAll("_", " ")}</span>;
}

export function DataTable({
  headers,
  rows,
}: {
  headers: string[];
  rows: ReactNode[][];
}) {
  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>{headers.map((h) => <th key={h}>{h}</th>)}</tr>
        </thead>
        <tbody>
          {rows.map((r, i) => (
            <tr key={i}>
              {r.map((cell, j) => (
                <td key={j}>{cell}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function ConfirmDialog({
  open,
  title,
  body,
  onClose,
  onConfirm,
  confirmLabel = "Xác nhận",
  cancelLabel = "Hủy",
}: {
  open: boolean;
  title: string;
  body?: ReactNode;
  onClose: () => void;
  onConfirm: () => void;
  confirmLabel?: string;
  cancelLabel?: string;
}) {
  if (!open) return null;
  return (
    <div className="dialog-backdrop">
      <div className="dialog">
        <h3>{title}</h3>
        {body ? <div className="dialog-body">{body}</div> : null}
        <div className="dialog-actions">
          <Button type="button" onClick={onClose}>{cancelLabel}</Button>
          <Button type="button" className="btn-primary" onClick={onConfirm}>
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  );
}
