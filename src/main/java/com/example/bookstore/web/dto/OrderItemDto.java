package com.example.bookstore.web.dto;

public record OrderItemDto(
        Long sachId,
        String tenSach,
        Integer soLuong,
        Long gia
) {
}

