package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.DonHang;
import com.example.bookstore.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DonHangRepository extends JpaRepository<DonHang, Long> {

    List<DonHang> findByKhachHangIdOrderByNgayDatDesc(Long khachHangId);

    List<DonHang> findByTrangThai(OrderStatus status);

    Optional<DonHang> findByIdAndKhachHangId(Long id, Long khachHangId);

    List<DonHang> findByNgayDatBetween(Instant from, Instant to);
}

