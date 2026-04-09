package com.example.bookstore.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CancelRequestCreate(
        @NotNull Long donHangId,
        @NotBlank String lyDoHuy
) {
}

