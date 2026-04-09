package com.example.bookstore.web.dto;

import jakarta.validation.constraints.NotNull;

public record MockAuthorizePaymentRequest(
        @NotNull Long orderId,
        @NotNull Result result
) {
    public enum Result {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        MAINTENANCE,
        USER_CANCELLED
    }
}

