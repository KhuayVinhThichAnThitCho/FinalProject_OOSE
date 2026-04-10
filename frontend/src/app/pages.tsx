import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api } from "../shared/api";
import { useAuthStore } from "../features/auth/authStore";
import { Button, Card, ConfirmDialog, EmptyState, Input, Select, StatusBadge, formatCurrency, formatDateTime, useToast } from "../shared/ui";
import type { Book, CancelRequest, CancelRequestDetail, OrderDetail, OrderSummary, SalesReportData } from "../shared/types";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

function useLoad<T>(loader: () => Promise<T>, deps: unknown[]) {
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
        const msg = e instanceof Error ? e.message : "Request failed";
        setError(msg);
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

export function LoginPage() {
  const [username, setUsername] = useState("customer");
  const [password, setPassword] = useState("password");
  const [loading, setLoading] = useState(false);
  const setAuth = useAuthStore((s) => s.setAuth);
  const roles = useAuthStore((s) => s.roles);
  const navigate = useNavigate();
  const { push } = useToast();

  useEffect(() => {
    if (roles.length > 0) navigate("/");
  }, [navigate, roles.length]);

  return (
    <div className="center-wrap">
      <Card className="auth-card">
        <h1>Bookstore Portal</h1>
        <p className="muted">JWT login with role-based access</p>
        <label>Username</label>
        <Input value={username} onChange={(e) => setUsername(e.target.value)} />
        <label>Password</label>
        <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <Button
          className="btn-primary"
          disabled={loading}
          onClick={async () => {
            setLoading(true);
            try {
              const res = await api.login(username, password);
              setAuth(res);
              push("Login successful", "success");
              navigate("/");
            } catch {
              push("Invalid credentials", "error");
            } finally {
              setLoading(false);
            }
          }}
        >
          {loading ? "Signing in..." : "Sign in"}
        </Button>
      </Card>
    </div>
  );
}

export function HomePage() {
  const roles = useAuthStore((s) => s.roles);
  if (roles.includes("MANAGER")) return <NavigateTo to="/manager/reports/sales" />;
  if (roles.includes("STAFF")) return <NavigateTo to="/staff/orders/pending" />;
  return <NavigateTo to="/books" />;
}

function NavigateTo({ to }: { to: string }) {
  const navigate = useNavigate();
  useEffect(() => {
    navigate(to, { replace: true });
  }, [navigate, to]);
  return null;
}

export function BooksPage() {
  const { data, loading, error } = useLoad(api.books, []);
  return (
    <div>
      <h2>Books</h2>
      {loading ? <p>Loading...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      <div className="grid">
        {data?.map((book) => (
          <Card key={book.id}>
            <h3>{book.title}</h3>
            <p>{book.category || "General"}</p>
            <p>{formatCurrency(book.price)}</p>
            <p className="muted">Stock: {book.stockQuantity}</p>
          </Card>
        ))}
      </div>
    </div>
  );
}

export function NewOrderPage() {
  const navigate = useNavigate();
  const customerId = useAuthStore((s) => s.customerId);
  const setCustomerId = useAuthStore((s) => s.setCustomerId);
  const [value, setValue] = useState(customerId ?? 1);
  const { push } = useToast();
  return (
    <Card>
      <h2>Create Order</h2>
      <p className="muted">Create draft order before confirm and checkout.</p>
      <label>Customer ID</label>
      <Input type="number" value={value} onChange={(e) => setValue(Number(e.target.value))} />
      <Button
        className="btn-primary"
        onClick={async () => {
          setCustomerId(value);
          try {
            const created = await api.createOrder(value);
            push(`Order #${created.orderId} created`, "success");
            navigate(`/orders/${created.orderId}/confirm`);
          } catch {
            push("Create order failed", "error");
          }
        }}
      >
        Create and Continue
      </Button>
    </Card>
  );
}

export function ConfirmOrderPage() {
  const params = useParams();
  const orderId = Number(params.orderId);
  const [books, setBooks] = useState<Book[]>([]);
  const [receiverName, setReceiverName] = useState("Nguyen Van A");
  const [receiverPhone, setReceiverPhone] = useState("0900000000");
  const [shippingAddress, setShippingAddress] = useState("HCM");
  const [shippingFee, setShippingFee] = useState(15000);
  const [items, setItems] = useState<Array<{ bookId: number; quantity: number }>>([]);
  const { push } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    api.books().then(setBooks).catch(() => setBooks([]));
  }, []);

  return (
    <Card>
      <h2>Confirm Order #{orderId}</h2>
      <p className="muted">Pick products, shipping info and confirm order.</p>
      <div className="grid two">
        <div>
          <label>Receiver name</label>
          <Input value={receiverName} onChange={(e) => setReceiverName(e.target.value)} />
        </div>
        <div>
          <label>Receiver phone</label>
          <Input value={receiverPhone} onChange={(e) => setReceiverPhone(e.target.value)} />
        </div>
      </div>
      <label>Address</label>
      <Input value={shippingAddress} onChange={(e) => setShippingAddress(e.target.value)} />
      <label>Shipping fee</label>
      <Input type="number" value={shippingFee} onChange={(e) => setShippingFee(Number(e.target.value))} />
      <label>Items</label>
      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr>
              <th>Book</th>
              <th>Price</th>
              <th>Qty</th>
            </tr>
          </thead>
          <tbody>
            {books.map((book) => {
              const existing = items.find((it) => it.bookId === book.id)?.quantity ?? 0;
              return (
                <tr key={book.id}>
                  <td>{book.title}</td>
                  <td>{formatCurrency(book.price)}</td>
                  <td>
                    <Input
                      type="number"
                      min={0}
                      value={existing}
                      onChange={(e) => {
                        const quantity = Number(e.target.value);
                        setItems((prev) => {
                          const next = prev.filter((p) => p.bookId !== book.id);
                          if (quantity > 0) next.push({ bookId: book.id, quantity });
                          return next;
                        });
                      }}
                    />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
      <Button
        className="btn-primary"
        onClick={async () => {
          try {
            await api.confirmOrder(orderId, { receiverName, receiverPhone, shippingAddress, shippingFee, items });
            push("Order confirmed", "success");
            navigate(`/orders/${orderId}/pay`);
          } catch {
            push("Confirm order failed", "error");
          }
        }}
      >
        Confirm order
      </Button>
    </Card>
  );
}

export function PayOrderPage() {
  const params = useParams();
  const orderId = Number(params.orderId);
  const [method, setMethod] = useState("ONLINE_OK");
  const [result, setResult] = useState<string>("");
  const { push } = useToast();
  const mapMethod = (value: string) => {
    if (value.startsWith("ONLINE")) return "ONLINE";
    return value;
  };
  return (
    <Card>
      <h2>Checkout #{orderId}</h2>
      <label>Payment method</label>
      <Select value={method} onChange={(e) => setMethod(e.target.value)}>
        <option value="ONLINE_OK">ONLINE_OK</option>
        <option value="ONLINE_NO_MONEY">ONLINE_NO_MONEY</option>
        <option value="ONLINE_MAINT">ONLINE_MAINT</option>
        <option value="ONLINE_CANCEL">ONLINE_CANCEL</option>
      </Select>
      <p className="muted">Backend currently handles payment via paymentMethodCode base mapping.</p>
      <Button
        className="btn-primary"
        onClick={async () => {
          try {
            const res = await api.checkout(orderId, mapMethod(method));
            setResult(`${res.status}: ${res.message}`);
            push(res.message, res.status === "PAID" ? "success" : "info");
          } catch {
            push("Checkout failed", "error");
          }
        }}
      >
        Confirm order and pay
      </Button>
      {result ? <p>{result}</p> : null}
    </Card>
  );
}

export function MyOrdersPage() {
  const { data, loading, error } = useLoad(api.myOrders, []);
  if (loading) return <p>Loading...</p>;
  if (error) return <p className="error">{error}</p>;
  if (!data || data.orders.length === 0) return <EmptyState title="No orders yet" desc={data?.message ?? "Please create your first order."} />;
  return (
    <Card>
      <h2>My Orders</h2>
      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr>
              <th>Order</th>
              <th>Date</th>
              <th>Total</th>
              <th>Status</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {data.orders.map((o) => (
              <tr key={o.orderId}>
                <td>#{o.orderId}</td>
                <td>{formatDateTime(o.ngayDat)}</td>
                <td>{formatCurrency(o.totalAmount)}</td>
                <td><StatusBadge status={o.status} /></td>
                <td><Link to={`/orders/${o.orderId}`}>Details</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}

export function OrderDetailPage() {
  const params = useParams();
  const orderId = Number(params.orderId);
  const customerId = useAuthStore((s) => s.customerId) ?? 1;
  const { data, loading, error } = useLoad(() => api.myOrderDetail(orderId, customerId), [orderId, customerId]);
  return <OrderDetailCard data={data} loading={loading} error={error} title={`Order #${orderId}`} />;
}

function OrderDetailCard({ data, loading, error, title }: { data: OrderDetail | null; loading: boolean; error: string | null; title: string }) {
  if (loading) return <p>Loading...</p>;
  if (error) return <p className="error">{error}</p>;
  if (!data) return <EmptyState title="No detail" desc="Cannot load order detail." />;
  return (
    <Card>
      <h2>{title}</h2>
      <p className="muted">{formatDateTime(data.ngayDat)}</p>
      <p>Total: {formatCurrency(data.totalAmount)}</p>
      <StatusBadge status={data.status} />
      <h3>Shipping</h3>
      <p>{data.shipping?.receiverName} - {data.shipping?.receiverPhone}</p>
      <p>{data.shipping?.address}</p>
      <h3>Items</h3>
      <ul>
        {data.items.map((i) => (
          <li key={i.bookId}>
            {i.title} x{i.quantity} - {formatCurrency(i.unitPrice)}
          </li>
        ))}
      </ul>
    </Card>
  );
}

export function NewCancelRequestPage() {
  const [orderId, setOrderId] = useState<number>(1);
  const [reason, setReason] = useState("Thay doi y dinh mua hang");
  const { push } = useToast();
  return (
    <Card>
      <h2>Create cancel request</h2>
      <label>Order ID</label>
      <Input type="number" value={orderId} onChange={(e) => setOrderId(Number(e.target.value))} />
      <label>Reason</label>
      <Input value={reason} onChange={(e) => setReason(e.target.value)} />
      <Button
        className="btn-primary"
        onClick={async () => {
          try {
            const id = await api.createCancelRequest(orderId, reason);
            push(`Cancel request #${id} created`, "success");
          } catch {
            push("Create cancel request failed", "error");
          }
        }}
      >
        Send request
      </Button>
    </Card>
  );
}

export function StaffPendingOrdersPage() {
  const { data, loading, error } = useLoad(api.staffPendingOrders, []);
  const [selected, setSelected] = useState<number | null>(null);
  const [open, setOpen] = useState(false);
  const { push } = useToast();
  if (loading) return <p>Loading...</p>;
  if (error) return <p className="error">{error}</p>;
  return (
    <Card>
      <h2>Pending Orders (PAID)</h2>
      {data && data.length === 0 ? <EmptyState title="No pending orders" desc="Nothing to confirm now." /> : null}
      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr><th>Order</th><th>Date</th><th>Total</th><th>Status</th><th /></tr>
          </thead>
          <tbody>
            {data?.map((o: OrderSummary) => (
              <tr key={o.orderId}>
                <td>#{o.orderId}</td>
                <td>{formatDateTime(o.ngayDat)}</td>
                <td>{formatCurrency(o.totalAmount)}</td>
                <td><StatusBadge status={o.status} /></td>
                <td>
                  <Link to={`/staff/orders/${o.orderId}`}>View</Link>
                  <Button
                    className="btn-primary ml8"
                    onClick={() => {
                      setSelected(o.orderId);
                      setOpen(true);
                    }}
                  >
                    Confirm
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <ConfirmDialog
        open={open}
        title="Confirm this order?"
        onClose={() => setOpen(false)}
        onConfirm={async () => {
          if (!selected) return;
          try {
            const msg = await api.staffConfirmOrder(selected);
            push(msg, "success");
          } catch {
            push("Confirm order failed", "error");
          } finally {
            setOpen(false);
          }
        }}
      />
    </Card>
  );
}

export function StaffOrderDetailPage() {
  const params = useParams();
  const orderId = Number(params.orderId);
  const { data, loading, error } = useLoad(() => api.staffOrderDetail(orderId), [orderId]);
  return <OrderDetailCard data={data} loading={loading} error={error} title={`Staff order #${orderId}`} />;
}

export function StaffCancelRequestsPage() {
  const [status, setStatus] = useState("");
  const { data, loading, error, setData } = useLoad(() => api.staffCancelRequests(status || undefined), [status]);
  useEffect(() => {
    api.staffCancelRequests(status || undefined).then(setData).catch(() => undefined);
  }, [setData, status]);
  if (loading) return <p>Loading...</p>;
  if (error) return <p className="error">{error}</p>;
  return (
    <Card>
      <h2>Cancel Request Queue</h2>
      <label>Status filter</label>
      <Select value={status} onChange={(e) => setStatus(e.target.value)}>
        <option value="">All</option>
        <option value="PENDING">PENDING</option>
        <option value="APPROVED">APPROVED</option>
        <option value="REJECTED">REJECTED</option>
      </Select>
      {!data || data.length === 0 ? <EmptyState title="No requests" desc="No cancel requests at the moment." /> : null}
      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr><th>ID</th><th>Order</th><th>Reason</th><th>Status</th><th /></tr>
          </thead>
          <tbody>
            {data?.map((r: CancelRequest) => (
              <tr key={r.id}>
                <td>#{r.id}</td>
                <td>#{r.order?.id}</td>
                <td>{r.reason}</td>
                <td><StatusBadge status={r.status} /></td>
                <td><Link to={`/staff/cancel-requests/${r.id}`}>Process</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}

export function StaffCancelRequestDetailPage() {
  const params = useParams();
  const id = Number(params.id);
  const { data, loading, error } = useLoad(() => api.staffCancelDetail(id), [id]);
  const [openApprove, setOpenApprove] = useState(false);
  const [openReject, setOpenReject] = useState(false);
  const { push } = useToast();
  if (loading) return <p>Loading...</p>;
  if (error || !data) return <p className="error">{error ?? "Not found"}</p>;
  const detail = data as CancelRequestDetail;
  return (
    <Card>
      <h2>Cancel Request #{detail.cancelRequestId}</h2>
      <p>{detail.reason}</p>
      <StatusBadge status={detail.status} />
      <OrderDetailCard data={detail.orderDetail} loading={false} error={null} title={`Order #${detail.orderDetail.orderId}`} />
      <div className="row">
        <Button className="btn-primary" onClick={() => setOpenApprove(true)}>Approve</Button>
        <Button onClick={() => setOpenReject(true)}>Reject</Button>
      </div>
      <ConfirmDialog
        open={openApprove}
        title="Approve cancel request?"
        onClose={() => setOpenApprove(false)}
        onConfirm={async () => {
          try {
            const msg = await api.approveCancel(id);
            push(msg, "success");
          } catch {
            push("Approve failed", "error");
          } finally {
            setOpenApprove(false);
          }
        }}
      />
      <ConfirmDialog
        open={openReject}
        title="Reject cancel request?"
        onClose={() => setOpenReject(false)}
        onConfirm={async () => {
          try {
            const msg = await api.rejectCancel(id);
            push(msg, "info");
          } catch {
            push("Reject failed", "error");
          } finally {
            setOpenReject(false);
          }
        }}
      />
    </Card>
  );
}

export function ManagerBooksPage() {
  const { data, loading, error } = useLoad(api.managerBooks, []);
  if (loading) return <p>Loading...</p>;
  if (error) return <p className="error">{error}</p>;
  return (
    <Card>
      <h2>Book Pricing Management</h2>
      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr><th>Book</th><th>Category</th><th>Sale</th><th>Cost</th><th /></tr>
          </thead>
          <tbody>
            {data?.map((b) => (
              <tr key={b.id}>
                <td>{b.title}</td>
                <td>{b.category ?? "-"}</td>
                <td>{formatCurrency(b.price)}</td>
                <td>{formatCurrency(b.costPrice)}</td>
                <td><Link to={`/manager/books/${b.id}/price`}>Update</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}

export function ManagerBookPricePage() {
  const params = useParams();
  const id = Number(params.id);
  const { data, loading, error } = useLoad(() => api.managerBookDetail(id), [id]);
  const [price, setPrice] = useState<number>(0);
  const [allowLossSale, setAllowLossSale] = useState(false);
  const [confirmLoss, setConfirmLoss] = useState(false);
  const { push } = useToast();
  useEffect(() => {
    if (data) setPrice(data.price);
  }, [data]);
  if (loading) return <p>Loading...</p>;
  if (error || !data) return <p className="error">{error ?? "Not found"}</p>;
  return (
    <Card>
      <h2>Update price: {data.title}</h2>
      <p>Cost: {formatCurrency(data.costPrice)}</p>
      <label>New sale price</label>
      <Input type="number" value={price} onChange={(e) => setPrice(Number(e.target.value))} />
      <label className="checkbox">
        <input type="checkbox" checked={allowLossSale} onChange={(e) => setAllowLossSale(e.target.checked)} />
        Allow loss sale
      </label>
      <Button
        className="btn-primary"
        onClick={async () => {
          try {
            const res = await api.updateBookPrice(id, {
              newSalePrice: price,
              effectiveFrom: new Date().toISOString(),
              allowLossSale,
            });
            if (!res.updated) {
              setConfirmLoss(true);
              return;
            }
            push(res.message, "success");
          } catch {
            push("Update price failed", "error");
          }
        }}
      >
        Update
      </Button>
      <ConfirmDialog
        open={confirmLoss}
        title="Price is below cost. Continue?"
        onClose={() => setConfirmLoss(false)}
        onConfirm={async () => {
          try {
            const res = await api.updateBookPrice(id, {
              newSalePrice: price,
              effectiveFrom: new Date().toISOString(),
              allowLossSale: true,
            });
            push(res.message, "success");
          } catch {
            push("Update price failed", "error");
          } finally {
            setConfirmLoss(false);
          }
        }}
      />
    </Card>
  );
}

export function ManagerSalesReportPage() {
  const [from, setFrom] = useState("2026-01-01T00:00:00Z");
  const [to, setTo] = useState(new Date().toISOString());
  const [category, setCategory] = useState("");
  const [status, setStatus] = useState("");
  const [data, setData] = useState<SalesReportData | null>(null);
  const [loading, setLoading] = useState(false);
  const { push } = useToast();
  const chartData = useMemo(
    () => (data?.topBooks ?? []).map((b) => ({ name: b.title.slice(0, 15), sold: b.quantitySold })),
    [data]
  );
  return (
    <Card>
      <h2>Sales Report</h2>
      <div className="grid two">
        <div>
          <label>From (ISO)</label>
          <Input value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div>
          <label>To (ISO)</label>
          <Input value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
      </div>
      <div className="grid two">
        <div>
          <label>Category</label>
          <Input value={category} onChange={(e) => setCategory(e.target.value)} />
        </div>
        <div>
          <label>Status</label>
          <Input value={status} onChange={(e) => setStatus(e.target.value)} />
        </div>
      </div>
      <div className="row">
        <Button
          className="btn-primary"
          disabled={loading}
          onClick={async () => {
            setLoading(true);
            try {
              const report = await api.salesReport(from, to, category || undefined, status || undefined);
              setData(report);
            } catch {
              push("Load report failed", "error");
            } finally {
              setLoading(false);
            }
          }}
        >
          {loading ? "Loading..." : "View Report"}
        </Button>
        <Button
          onClick={async () => {
            try {
              const blob = await api.exportReport("xlsx", from, to, category || undefined, status || undefined);
              const url = URL.createObjectURL(blob);
              const a = document.createElement("a");
              a.href = url;
              a.download = "sales-report.xlsx";
              a.click();
              URL.revokeObjectURL(url);
              push("Report exported", "success");
            } catch {
              push("Export failed", "error");
            }
          }}
        >
          Export XLSX
        </Button>
        <Button
          onClick={async () => {
            try {
              const blob = await api.exportReport("pdf", from, to, category || undefined, status || undefined);
              const url = URL.createObjectURL(blob);
              const a = document.createElement("a");
              a.href = url;
              a.download = "sales-report.pdf";
              a.click();
              URL.revokeObjectURL(url);
              push("Report exported", "success");
            } catch {
              push("Export failed", "error");
            }
          }}
        >
          Export PDF
        </Button>
      </div>
      {data ? (
        <>
          <div className="grid three">
            <Card><h3>Total orders</h3><p>{data.totalOrders}</p></Card>
            <Card><h3>Total revenue</h3><p>{formatCurrency(data.totalRevenue)}</p></Card>
            <Card><h3>Growth</h3><p>{data.growthPercent == null ? "N/A" : `${data.growthPercent.toFixed(2)}%`}</p></Card>
          </div>
          <div className="chart-card">
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="sold" fill="#6d5efc" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </>
      ) : null}
    </Card>
  );
}

export function UnauthorizedPage() {
  return (
    <Card>
      <h2>Unauthorized</h2>
      <p>You do not have permission to access this page.</p>
      <Link to="/">Go home</Link>
    </Card>
  );
}

export function NotFoundPage() {
  return (
    <Card>
      <h2>404</h2>
      <p>Page not found.</p>
      <Link to="/">Go home</Link>
    </Card>
  );
}
