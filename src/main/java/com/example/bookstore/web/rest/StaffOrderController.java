package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.Order;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.web.dto.OrderDetailView;
import com.example.bookstore.web.dto.OrderItemDto;
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
    public List<OrderSummaryResponse> viewOrderList() {
        return orderRepository.findByStatus(OrderStatus.PAID).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getOrderedAt(), o.getTotalAmount(), o.getStatus()))
                .toList();
    }

    @GetMapping("/{id}")
    public OrderDetailView viewOrderDetail(@PathVariable("id") Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin đơn hàng."));

        List<OrderItemDto> items = o.getOrderDetails().stream()
                .map(i -> new OrderItemDto(
                        i.getBookInfo().getId(),
                        i.getBookInfo().getTitle(),
                        i.getQuantity(),
                        i.getUnitPrice()
                ))
                .toList();

        OrderDetailView.ShippingInfo shipping = o.getShippingInfo() == null ? null :
                new OrderDetailView.ShippingInfo(
                        o.getShippingInfo().getAddress(),
                        o.getShippingInfo().getReceiverName(),
                        o.getShippingInfo().getReceiverPhone(),
                        o.getShippingInfo().getShippingStatus()
                );

        return new OrderDetailView(
                o.getId(),
                o.getOrderedAt(),
                o.getTotalAmount(),
                o.getStatus(),
                shipping,
                items
        );
    }

    @PostMapping("/{id}/confirm")
    public String confirmOrder(@PathVariable("id") Long orderId) {
        return orderService.confirmOrder(orderId).message();
    }

    @PostMapping("/{id}/cancel-processing")
    public String cancelProcessing(@PathVariable("id") Long orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin đơn hàng."));
        return "Đã hủy xác nhận đơn hàng!";
    }
}

