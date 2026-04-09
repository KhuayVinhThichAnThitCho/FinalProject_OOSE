package com.example.bookstore.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockPaymentOutcomeStore {

    private final ConcurrentHashMap<Long, PaymentResultStatus> outcomes = new ConcurrentHashMap<>();

    public void setOutcome(Long orderId, PaymentResultStatus status) {
        if (orderId == null) throw new IllegalArgumentException("orderId is required");
        if (status == null) throw new IllegalArgumentException("status is required");
        outcomes.put(orderId, status);
    }

    public PaymentResultStatus consumeOutcome(Long orderId) {
        if (orderId == null) return null;
        return outcomes.remove(orderId);
    }
}

