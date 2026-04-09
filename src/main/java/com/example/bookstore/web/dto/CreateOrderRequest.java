package com.example.bookstore.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull Long customerId,
        @NotEmpty List<Item> items,
        @NotBlank String receiverName,
        @NotBlank String receiverPhone,
        @NotBlank String shippingAddress,
        @NotNull @Min(0) Long shippingFee
) {
    public record Item(
            @NotNull Long bookId,
            @NotNull @Min(1) Integer quantity
    ) {
    }
}

