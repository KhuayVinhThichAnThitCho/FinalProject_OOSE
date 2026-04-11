package com.example.bookstore.web.dto;

import com.example.bookstore.domain.enums.OrderStatus;

import java.time.Instant;
import java.util.List;

/**
 * REST view of an order; distinct from domain entity {@link com.example.bookstore.domain.entity.OrderDetail}.
 */
public record OrderDetailView(
        Long orderId,
        Instant ngayDat,
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
