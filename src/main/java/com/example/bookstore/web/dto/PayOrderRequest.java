package com.example.bookstore.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PayOrderRequest(
        @NotBlank String paymentMethodCode
) {
}

