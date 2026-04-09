package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.ThanhToan;
import com.example.bookstore.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long> {

    List<ThanhToan> findByDonHangId(Long donHangId);

    List<ThanhToan> findByTrangThai(PaymentStatus status);
}

