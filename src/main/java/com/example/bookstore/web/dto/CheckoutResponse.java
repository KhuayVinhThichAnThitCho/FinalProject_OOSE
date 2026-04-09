package com.example.bookstore.web.dto;

import com.example.bookstore.domain.enums.OrderStatus;

public record CheckoutResponse(
        Long orderId,
        OrderStatus orderStatus,
        String message
) {
}

