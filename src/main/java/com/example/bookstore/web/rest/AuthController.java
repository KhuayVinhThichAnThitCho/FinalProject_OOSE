package com.example.bookstore.web.rest;

import com.example.bookstore.service.AuthService;
import com.example.bookstore.web.dto.LoginRequest;
import com.example.bookstore.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        AuthService.AuthResult res = authService.login(req.username(), req.password());
        return new LoginResponse(res.token(), "Bearer", res.username(), res.roles());
    }
}

