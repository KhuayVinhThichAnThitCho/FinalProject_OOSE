import { useEffect, useState } from "react";
import { getErrorMessage } from "../lib/error";

export function useLoad<T>(loader: () => Promise<T>, deps: unknown[]) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    setLoading(true);
    loader()
      .then((res) => {
        if (!mounted) return;
        setData(res);
        setError(null);
      })
      .catch((e: unknown) => {
        if (!mounted) return;
        setError(getErrorMessage(e, "Không tải được dữ liệu"));
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });
    return () => {
      mounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  return { data, loading, error, setData };
}
