package com.example.bookstore.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    private final MockPaymentOutcomeStore outcomeStore;

    public MockPaymentGateway(MockPaymentOutcomeStore outcomeStore) {
        this.outcomeStore = outcomeStore;
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // Demo/mock gateway: outcome is set by /api/payment/mock/authorize (per-order),
        // fallback to SUCCESS when not specified.
        PaymentResultStatus decided = outcomeStore.consumeOutcome(request.orderId());
        if (decided == null) {
            decided = PaymentResultStatus.SUCCESS;
        }

        return switch (decided) {
            case SUCCESS -> new PaymentResult(PaymentResultStatus.SUCCESS, "MOCK-" + UUID.randomUUID(), "Thanh toán thành công");
            case INSUFFICIENT_FUNDS -> new PaymentResult(PaymentResultStatus.INSUFFICIENT_FUNDS, null, "Số dư không đủ");
            case MAINTENANCE -> new PaymentResult(PaymentResultStatus.MAINTENANCE, null, "Cổng thanh toán đang bảo trì");
            case USER_CANCELLED -> new PaymentResult(PaymentResultStatus.USER_CANCELLED, null, "Người dùng hủy thanh toán");
        };
    }
}

