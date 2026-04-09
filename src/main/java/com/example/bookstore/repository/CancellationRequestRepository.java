package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.CancellationRequest;
import com.example.bookstore.domain.enums.CancelRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CancellationRequestRepository extends JpaRepository<CancellationRequest, Long> {
    Optional<CancellationRequest> findByOrderId(Long orderId);
    List<CancellationRequest> findByStatus(CancelRequestStatus status);
}

