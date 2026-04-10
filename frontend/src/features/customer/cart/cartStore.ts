import { create } from "zustand";

export type CartItem = {
  bookId: number;
  title: string;
  price: number;
  quantity: number;
};

type CartState = {
  items: CartItem[];
  addItem: (book: { id: number; title: string; price: number }, qty?: number) => void;
  removeItem: (bookId: number) => void;
  updateQty: (bookId: number, qty: number) => void;
  clear: () => void;
};

const CART_KEY = "bookstore_cart";

const load = (): CartItem[] => {
  try {
    const raw = localStorage.getItem(CART_KEY);
    return raw ? (JSON.parse(raw) as CartItem[]) : [];
  } catch {
    return [];
  }
};

const persist = (items: CartItem[]) =>
  localStorage.setItem(CART_KEY, JSON.stringify(items));

export const useCartStore = create<CartState>((set) => ({
  items: load(),

  addItem: (book, qty = 1) =>
    set((s) => {
      const existing = s.items.find((i) => i.bookId === book.id);
      const next = existing
        ? s.items.map((i) =>
            i.bookId === book.id ? { ...i, quantity: i.quantity + qty } : i,
          )
        : [...s.items, { bookId: book.id, title: book.title, price: book.price, quantity: qty }];
      persist(next);
      return { items: next };
    }),

  removeItem: (bookId) =>
    set((s) => {
      const next = s.items.filter((i) => i.bookId !== bookId);
      persist(next);
      return { items: next };
    }),

  updateQty: (bookId, qty) =>
    set((s) => {
      const next =
        qty <= 0
          ? s.items.filter((i) => i.bookId !== bookId)
          : s.items.map((i) => (i.bookId === bookId ? { ...i, quantity: qty } : i));
      persist(next);
      return { items: next };
    }),

  clear: () => {
    persist([]);
    set({ items: [] });
  },
}));

export const selectItemCount = (s: CartState) =>
  s.items.reduce((sum, i) => sum + i.quantity, 0);

export const selectSubtotal = (s: CartState) =>
  s.items.reduce((sum, i) => sum + i.price * i.quantity, 0);
