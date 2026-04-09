package com.example.bookstore.web.dto;

import java.util.List;

public record OrderListResponse(
        List<OrderSummaryResponse> orders,
        String message
) {
}

