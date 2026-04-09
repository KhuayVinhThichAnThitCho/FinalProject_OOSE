package com.example.bookstore.service;

import com.example.bookstore.domain.entity.CancellationRequest;
import com.example.bookstore.domain.entity.Order;
import com.example.bookstore.domain.enums.CancelRequestStatus;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.repository.CancellationRequestRepository;
import com.example.bookstore.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class CancelRequestService {

    private final OrderRepository orderRepository;
    private final CancellationRequestRepository cancellationRequestRepository;

    public CancelRequestService(OrderRepository orderRepository, CancellationRequestRepository cancellationRequestRepository) {
        this.orderRepository = orderRepository;
        this.cancellationRequestRepository = cancellationRequestRepository;
    }

    @Transactional
    public CancellationRequest create(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        cancellationRequestRepository.findByOrderId(orderId).ifPresent(x -> {
            throw new IllegalStateException("Order already has a cancellation request");
        });

        if (order.getStatus() == OrderStatus.SHIPPING || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel this order");
        }

        if (order.getOrderedAt() != null) {
            Duration age = Duration.between(order.getOrderedAt(), Instant.now());
            if (age.toHours() > 24) {
                throw new IllegalStateException("Cancellation window expired");
            }
        }

        CancellationRequest req = new CancellationRequest();
        req.setOrder(order);
        req.setReason(reason);
        req.setRequestedAt(Instant.now());
        req.setStatus(CancelRequestStatus.PENDING);
        return cancellationRequestRepository.save(req);
    }

    @Transactional
    public String approve(Long requestId) {
        CancellationRequest req = cancellationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        Order order = req.getOrder();
        if (order.getStatus() == OrderStatus.SHIPPING || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel this order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        req.setStatus(CancelRequestStatus.APPROVED);

        return "Hủy đơn hàng thành công";
    }

    @Transactional
    public String reject(Long requestId) {
        CancellationRequest req = cancellationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        req.setStatus(CancelRequestStatus.REJECTED);
        return "Yêu cầu hủy đã bị từ chối";
    }
}

