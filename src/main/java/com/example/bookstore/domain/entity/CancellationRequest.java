package com.example.bookstore.domain.entity;

import com.example.bookstore.domain.enums.CancelRequestStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "cancellation_requests")
public class CancellationRequest extends AuditableEntity {

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
}

