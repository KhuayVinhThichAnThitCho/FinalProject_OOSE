package com.example.bookstore.service;

import com.example.bookstore.domain.entity.CancelRequest;
import com.example.bookstore.domain.entity.Order;
import com.example.bookstore.repository.CancelRequestRepository;
import com.example.bookstore.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelRequestService {

    private final OrderRepository orderRepository;
    private final CancelRequestRepository cancelRequestRepository;

    public CancelRequestService(OrderRepository orderRepository, CancelRequestRepository cancelRequestRepository) {
        this.orderRepository = orderRepository;
        this.cancelRequestRepository = cancelRequestRepository;
    }

    @Transactional
    public CancelRequest create(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        cancelRequestRepository.findByOrderId(orderId).ifPresent(x -> {
            throw new IllegalStateException("Order already has a cancellation request");
        });

        CancelRequest req = CancelRequest.createFor(order, reason);
        return cancelRequestRepository.save(req);
    }

    @Transactional
    public String approve(Long requestId) {
        CancelRequest req = cancelRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        req.approve();
        cancelRequestRepository.save(req);
        orderRepository.save(req.getOrder());
        return "Hủy đơn hàng thành công";
    }

    @Transactional
    public String reject(Long requestId) {
        CancelRequest req = cancelRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        req.reject();
        cancelRequestRepository.save(req);
        return "Yêu cầu hủy đã bị từ chối";
    }


    @Transactional(readOnly = true)
    public CancelRequest viewCancelRequest(Long cancelId) {
        return cancelRequestRepository.findById(cancelId)
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

