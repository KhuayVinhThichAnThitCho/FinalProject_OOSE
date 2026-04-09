package com.example.bookstore.web.rest;

import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.web.dto.OrderSummaryResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/orders")
@PreAuthorize("hasAnyRole('STAFF','MANAGER')")
public class StaffOrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public StaffOrderController(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @GetMapping("/pending")
    public List<OrderSummaryResponse> listPending() {
        return orderRepository.findByStatus(OrderStatus.PAID).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getOrderedAt(), o.getTotalAmount(), o.getStatus()))
                .toList();
    }

    @PostMapping("/{id}/confirm")
    public String confirmOrder(@PathVariable("id") Long orderId) {
        return orderService.startShipping(orderId).message();
    }

    @PostMapping("/{id}/cancel-processing")
    public String cancelProcessing(@PathVariable("id") Long orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        // Keep status unchanged by spec.
        return "Đã hủy xử lý đơn hàng!";
    }
}

