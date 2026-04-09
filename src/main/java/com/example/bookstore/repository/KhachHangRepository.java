package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {

    Optional<KhachHang> findByEmail(String email);
}

