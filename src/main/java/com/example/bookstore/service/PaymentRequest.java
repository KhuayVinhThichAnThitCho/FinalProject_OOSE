package com.example.bookstore.service;

public record PaymentRequest(
        Long orderId,
        Long amount,
        String methodCode,
        String username
) {
}

