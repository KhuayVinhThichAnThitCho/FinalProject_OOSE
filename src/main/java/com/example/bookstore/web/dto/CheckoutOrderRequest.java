package com.example.bookstore.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutOrderRequest(
        @NotBlank String paymentMethodCode
) {
}

