package com.example.bookstore.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult charge(PaymentRequest request) {
        // For demo: decide by methodCode suffix:
        // - ONLINE_OK -> success
        // - ONLINE_NO_MONEY -> insufficient funds
        // - ONLINE_MAINT -> maintenance
        // - ONLINE_CANCEL -> user cancelled
        // Otherwise: success
        String code = request.methodCode() == null ? "" : request.methodCode().toUpperCase();
        if (code.endsWith("_NO_MONEY")) {
            return new PaymentResult(PaymentResultStatus.INSUFFICIENT_FUNDS, null, "Số dư không đủ");
        }
        if (code.endsWith("_MAINT")) {
            return new PaymentResult(PaymentResultStatus.MAINTENANCE, null, "Cổng thanh toán đang bảo trì");
        }
        if (code.endsWith("_CANCEL")) {
            return new PaymentResult(PaymentResultStatus.USER_CANCELLED, null, "Người dùng hủy thanh toán");
        }
        return new PaymentResult(PaymentResultStatus.SUCCESS, "MOCK-" + UUID.randomUUID(), "Thanh toán thành công");
    }
}

