import axios from "axios";
import JSONbig from "json-bigint";
import { useAuthStore } from "../../features/auth/authStore";

const jsonParser = JSONbig({ storeAsString: true });

export const apiClient = axios.create({
  baseURL: "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
  transformResponse: [
    (data) => {
      if (typeof data !== "string" || data.length === 0) return data;
      try {
        return jsonParser.parse(data);
      } catch {
        return data;
      }
    },
  ],
});

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const fallback = "Có lỗi xảy ra khi gọi API";
    const data = error?.response?.data;
    const message =
      data?.message ||
      data?.error ||
      (typeof data === "string" ? data : null) ||
      error?.message ||
      fallback;
    return Promise.reject(new Error(message));
  }
);
