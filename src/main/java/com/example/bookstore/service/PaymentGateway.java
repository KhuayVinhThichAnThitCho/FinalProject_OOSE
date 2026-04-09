package com.example.bookstore.service;

public interface PaymentGateway {

    PaymentResult charge(PaymentRequest request);
}

