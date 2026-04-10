/* eslint-disable react-refresh/only-export-components */
import type { PropsWithChildren } from "react";
import { createContext, useContext, useMemo, useState } from "react";

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
        }, 3000);
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
