package com.example.bookstore.domain.entity;

import com.example.bookstore.domain.enums.CancelRequestStatus;
import com.example.bookstore.domain.enums.OrderStatus;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "cancellation_requests")
public class CancelRequest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(50)")
    private CancelRequestStatus status;

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public CancelRequest getCancelRequest() {
        return this;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public CancelRequestStatus getStatus() {
        return status;
    }

    public void setStatus(CancelRequestStatus status) {
        this.status = status;
    }

    public static CancelRequest createFor(Order order, String reason) {
        OrderStatus os = order.getStatus();
        if (os == OrderStatus.SHIPPING || os == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel this order");
        }
        if (order.getOrderedAt() != null) {
            Duration age = Duration.between(order.getOrderedAt(), Instant.now());
            if (age.toHours() > 24) {
                throw new IllegalStateException("Đã quá thời gian cho phép hủy đơn");
            }
        }

        CancelRequest req = new CancelRequest();
        req.setOrder(order);
        req.setReason(reason);
        req.setRequestedAt(Instant.now());
        req.setStatus(CancelRequestStatus.PENDING);
        return req;
    }

    public void approve() {
        if (this.status != CancelRequestStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu hủy đã được xử lý trước đó");
        }

        Order o = this.order;
        if (o.getOrderedAt() != null) {
            Duration age = Duration.between(o.getOrderedAt(), Instant.now());
            if (age.toHours() > 24) {
                throw new IllegalStateException("Đã quá thời gian cho phép hủy đơn");
            }
        }
        if (o.getStatus() == OrderStatus.SHIPPING || o.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Không thể hủy đơn hàng này");
        }

        o.updateStatus(OrderStatus.CANCELLED);
        this.status = CancelRequestStatus.APPROVED;
    }

    public void reject() {
        if (this.status != CancelRequestStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu hủy đã được xử lý trước đó");
        }
        this.status = CancelRequestStatus.REJECTED;
    }
}

