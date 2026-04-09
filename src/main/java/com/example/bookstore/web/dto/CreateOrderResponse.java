package com.example.bookstore.web.dto;

import com.example.bookstore.domain.enums.OrderStatus;

import java.time.Instant;
import java.util.List;

public record CreateOrderResponse(
        Long orderId,
        Instant orderedAt,
        Long shippingFee,
        Long totalAmount,
        OrderStatus status,
        ShippingInfo shipping,
        List<OrderItemDto> items
) {
    public record ShippingInfo(
            String address,
            String receiverName,
            String receiverPhone,
            String shippingStatus
    ) {
    }
}

