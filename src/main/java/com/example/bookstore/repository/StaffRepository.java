package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
}

