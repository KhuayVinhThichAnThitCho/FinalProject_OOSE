import { create } from "zustand";
import type { LoginResponse, Role } from "../../shared/types";

type AuthState = {
  token: string | null;
  username: string | null;
  roles: Role[];
  customerId: number | null;
  setAuth: (payload: LoginResponse) => void;
  setCustomerId: (id: number) => void;
  logout: () => void;
};

const AUTH_KEY = "bookstore_auth";

type PersistedAuth = Pick<AuthState, "token" | "username" | "roles" | "customerId">;

const inferCustomerId = (username: string, roles: Role[]): number | null => {
  if (!roles.includes("CUSTOMER")) return null;
  if (username === "customer") return 1;
  return null;
};

const loadInitial = (): PersistedAuth => {
  const raw = localStorage.getItem(AUTH_KEY);
  if (!raw) return { token: null, username: null, roles: [], customerId: null };
  try {
    return JSON.parse(raw) as PersistedAuth;
  } catch {
    return { token: null, username: null, roles: [], customerId: null };
  }
};

const persist = (state: PersistedAuth) => {
  localStorage.setItem(AUTH_KEY, JSON.stringify(state));
};

const initial = loadInitial();

export const useAuthStore = create<AuthState>((set) => ({
  token: initial.token,
  username: initial.username,
  roles: initial.roles,
  customerId: initial.customerId,
  setAuth: (payload) => {
    const next = {
      token: payload.token,
      username: payload.username,
      roles: payload.roles,
      customerId: inferCustomerId(payload.username, payload.roles),
    };
    persist(next);
    set(next);
  },
  setCustomerId: (id) => {
    set((state) => {
      const next = { ...state, customerId: id };
      persist(next);
      return next;
    });
  },
  logout: () => {
    const next = { token: null, username: null, roles: [], customerId: null };
    persist(next);
    set(next);
  },
}));
