package com.example.bookstore.domain.entity;

import com.example.bookstore.domain.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(50)")
    private PaymentStatus status;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt;

    @Column(name = "partner_transaction_id")
    private String partnerTransactionId;

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public String getPartnerTransactionId() {
        return partnerTransactionId;
    }

    public void setPartnerTransactionId(String partnerTransactionId) {
        this.partnerTransactionId = partnerTransactionId;
    }
}

