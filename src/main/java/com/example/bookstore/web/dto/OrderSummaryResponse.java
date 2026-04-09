package com.example.bookstore.web.dto;

import com.example.bookstore.domain.enums.OrderStatus;

import java.time.Instant;

public record OrderSummaryResponse(
        Long orderId,
        Instant ngayDat,
        Long totalAmount,
        OrderStatus status
) {
}

