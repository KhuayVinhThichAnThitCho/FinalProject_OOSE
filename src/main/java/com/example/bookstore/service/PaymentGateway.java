package com.example.bookstore.service;

public interface PaymentGateway {

    PaymentResult processPayment(PaymentRequest request);
}

