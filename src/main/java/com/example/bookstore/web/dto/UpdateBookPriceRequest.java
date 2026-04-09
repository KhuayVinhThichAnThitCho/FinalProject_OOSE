package com.example.bookstore.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateBookPriceRequest(
        @NotNull @Min(1) Long giaBanMoi,
        boolean chapNhanBanLo
) {
}

