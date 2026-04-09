package com.example.bookstore.web.dto;

import com.example.bookstore.service.PaymentResultStatus;

public record MockAuthorizePaymentResponse(
        Long orderId,
        PaymentResultStatus status,
        String message
) {
}

