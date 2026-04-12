package com.example.bookstore.web.dto;

import java.time.Instant;

public record CancelRequestSummaryResponse(
        Long id,
        OrderRef order,
        String reason,
        String status,
        Instant requestedAt
) {
    public record OrderRef(Long id, String status) {}
}
