package com.example.bookstore.web.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String tokenType,
        String username,
        List<String> roles
) {
}

