package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
}

