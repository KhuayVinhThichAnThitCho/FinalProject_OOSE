package com.example.bookstore.domain.entity;

import com.example.bookstore.domain.enums.OrderStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "processed_by_staff_id")
    private Staff processedBy;

    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount = 0L;

    @Column(name = "shipping_fee", nullable = false)
    private Long shippingFee = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(50)")
    private OrderStatus status;

    @Version
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShippingInfo shippingInfo;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private CancelRequest cancelRequest;

    public Long getId() {
        return id;
    }

    public void setOrderId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Staff getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Staff processedBy) {
        this.processedBy = processedBy;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public void setOrderDate(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Long shippingFee) {
        this.shippingFee = shippingFee == null ? 0L : shippingFee;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getOrderStatus() {
        return status;
    }

    public boolean isOrderStatusPending() {
        return status == OrderStatus.PAID;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public ShippingInfo getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(ShippingInfo shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public CancelRequest getCancelRequest() {
        return cancelRequest;
    }

    public void setCancelRequest(CancelRequest cancelRequest) {
        this.cancelRequest = cancelRequest;
    }

    public static Order makeNewOrder(Customer customer) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        order.setCustomer(customer);
        order.setOrderDate(Instant.now());
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    public void confirmOrder(Map<Long, Integer> quantities, List<Book> books, Long shippingFee) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }
        if (this.orderDetails != null && !this.orderDetails.isEmpty()) {
            throw new IllegalStateException("Order was already confirmed");
        }
        if (books.size() != quantities.size()) {
            throw new IllegalArgumentException("Book not found");
        }

        long sf = shippingFee == null ? 0L : shippingFee;
        if (sf < 0) {
            throw new IllegalArgumentException("Invalid shipping fee");
        }

        long subtotal = 0L;
        List<OrderDetail> lines = new ArrayList<>();
        for (Book book : books) {
            int qty = quantities.get(book.getId());
            book.checkStock(qty);

            subtotal += book.getPrice() * (long) qty;

            OrderDetail line = new OrderDetail();
            line.setOrder(this);
            line.setBook(book);
            line.setQuantity(qty);
            line.setUnitPrice(book.getPrice());
            lines.add(line);
        }
        this.orderDetails.addAll(lines);

        this.shippingFee = sf;
        this.totalAmount = subtotal + sf;
        this.status = OrderStatus.PROCESSING;
    }

    public void attachShipping(String receiverName, String receiverPhone, String address) {
        ShippingInfo si = new ShippingInfo();
        si.setOrder(this);
        si.setReceiverName(receiverName);
        si.setReceiverPhone(receiverPhone);
        si.setAddress(address);
        si.setShippingStatus("PENDING");
        this.shippingInfo = si;
    }

    public boolean isPayable() {
        boolean hasLines = orderDetails != null && !orderDetails.isEmpty();
        if (!hasLines) {
            return false;
        }
        return status == OrderStatus.PENDING || status == OrderStatus.PROCESSING;
    }

    public void markPaid(String partnerTransactionId) {
        this.status = OrderStatus.PAID;
    }

    public void markPaymentFailed() {
        this.status = OrderStatus.PROCESSING;
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            return;
        }
        if (status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Không thể hủy đơn hàng đã giao");
        }
        if (status == OrderStatus.SHIPPING) {
            throw new IllegalStateException("Không thể hủy đơn hàng đang giao");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void startShipping() {
        if (!isOrderStatusPending()) {
            throw new IllegalStateException("Đơn hàng đã được xác nhận");
        }
        for (OrderDetail line : this.orderDetails) {
            line.getBook().checkStock(line.getQuantity());
        }
        this.status = OrderStatus.SHIPPING;
    }

    public void markDelivered() {
        if (this.status != OrderStatus.SHIPPING) {
            throw new IllegalStateException("Chỉ đơn đang giao hàng (SHIPPING) mới có thể xác nhận đã giao thành công.");
        }
        this.status = OrderStatus.DELIVERED;
        if (this.shippingInfo != null) {
            this.shippingInfo.setShippingStatus("DELIVERED");
        }
    }
}

