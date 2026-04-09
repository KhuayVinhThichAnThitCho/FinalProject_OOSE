package com.example.bookstore.service;

import com.example.bookstore.config.JwtService;
import com.example.bookstore.domain.entity.UserAccount;
import com.example.bookstore.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResult login(String username, String password) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Sai tài khoản hoặc mật khẩu"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Sai tài khoản hoặc mật khẩu");
        }

        List<String> roles = user.getRoles() == null ? List.of() : new ArrayList<>(user.getRoles());
        String token = jwtService.generate(username, roles);
        return new AuthResult(token, username, roles);
    }

    public record AuthResult(String token, String username, List<String> roles) {}
}

