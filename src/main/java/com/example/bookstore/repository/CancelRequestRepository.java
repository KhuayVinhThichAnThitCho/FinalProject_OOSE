package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.CancelRequest;
import com.example.bookstore.domain.enums.CancelRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CancelRequestRepository extends JpaRepository<CancelRequest, Long> {
    Optional<CancelRequest> findByOrderId(Long orderId);
    List<CancelRequest> findByStatus(CancelRequestStatus status);
}

