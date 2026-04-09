package com.example.bookstore.service;

public record PaymentResult(
        PaymentResultStatus status,
        String partnerTransactionId,
        String message
) {
}

