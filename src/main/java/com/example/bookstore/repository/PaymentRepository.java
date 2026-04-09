package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.Payment;
import com.example.bookstore.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByStatus(PaymentStatus status);
}

