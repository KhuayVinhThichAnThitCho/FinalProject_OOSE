package com.example.bookstore.web.dto;

import com.example.bookstore.domain.enums.CancelRequestStatus;

import java.time.Instant;

public record CancelRequestDetailResponse(
        Long cancelRequestId,
        CancelRequestStatus status,
        String reason,
        Instant requestedAt,
        OrderDetailView orderDetail
) {
}

