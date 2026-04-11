package com.example.bookstore.service;

import com.example.bookstore.domain.entity.CancellationRequest;
import com.example.bookstore.domain.entity.Order;
import com.example.bookstore.repository.CancellationRequestRepository;
import com.example.bookstore.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        CancellationRequest req = CancellationRequest.createFor(order, reason);
        return cancellationRequestRepository.save(req);
    }

    @Transactional
    public String approve(Long requestId) {
        CancellationRequest req = cancellationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        req.approve();
        cancellationRequestRepository.save(req);
        orderRepository.save(req.getOrder());
        return "Hủy đơn hàng thành công";
    }

    @Transactional
    public String reject(Long requestId) {
        CancellationRequest req = cancellationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        req.reject();
        cancellationRequestRepository.save(req);
        return "Yêu cầu hủy đã bị từ chối";
    }


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

