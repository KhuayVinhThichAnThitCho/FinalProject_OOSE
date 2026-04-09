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

        if (req.getStatus() != CancelRequestStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu hủy đã được xử lý trước đó");
        }

        Order order = req.getOrder();
        if (order.getOrderedAt() != null) {
            Duration age = Duration.between(order.getOrderedAt(), Instant.now());
            if (age.toHours() > 24) {
                throw new IllegalStateException("Đã quá thời gian cho phép hủy đơn");
            }
        }
        if (order.getStatus() == OrderStatus.SHIPPING || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Không thể hủy đơn hàng này");
        }

        order.updateStatus(OrderStatus.CANCELLED);
        req.setStatus(CancelRequestStatus.APPROVED);
        orderRepository.save(order);
        cancellationRequestRepository.save(req);

        return "Hủy đơn hàng thành công";
    }

    @Transactional
    public String reject(Long requestId) {
        CancellationRequest req = cancellationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (req.getStatus() != CancelRequestStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu hủy đã được xử lý trước đó");
        }
        req.setStatus(CancelRequestStatus.REJECTED);
        cancellationRequestRepository.save(req);
        return "Yêu cầu hủy đã bị từ chối";
    }

    // ---- Methods named exactly like sequence diagrams ----

    @Transactional(readOnly = true)
    public CancellationRequest viewCancelRequest(Long cancelId) {
        return cancellationRequestRepository.findById(cancelId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hủy"));
    }

    @Transactional
    public String confirmCancelRequest(Long cancelId) {
        return approve(cancelId);
    }

    @Transactional
    public String rejectCancelRequest(Long cancelId) {
        return reject(cancelId);
    }
}

