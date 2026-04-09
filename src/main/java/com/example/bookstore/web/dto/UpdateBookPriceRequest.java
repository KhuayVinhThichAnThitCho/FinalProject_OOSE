package com.example.bookstore.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateBookPriceRequest(
        @NotNull @Min(1) Long newSalePrice,
        Instant effectiveFrom,
        boolean allowLossSale
) {
}

