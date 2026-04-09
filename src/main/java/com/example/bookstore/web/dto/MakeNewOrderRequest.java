package com.example.bookstore.web.dto;

import jakarta.validation.constraints.NotNull;

public record MakeNewOrderRequest(
        @NotNull Long customerId
) {
}

