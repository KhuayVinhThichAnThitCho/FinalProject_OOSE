package com.example.bookstore.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckoutRequest(
        @NotNull Long khachHangId,
        @NotEmpty List<Item> items,
        @NotBlank String nguoiNhan,
        @NotBlank String soDienThoaiNhan,
        @NotBlank String diaChiGiaoHang,
        @NotBlank String paymentMethodCode
) {
    public record Item(
            @NotNull Long sachId,
            @NotNull @Min(1) Integer soLuong
    ) {
    }
}

