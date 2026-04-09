package com.example.bookstore.web.dto;

public record OrderItemDto(
        Long bookId,
        String title,
        Integer quantity,
        Long unitPrice
) {
}

