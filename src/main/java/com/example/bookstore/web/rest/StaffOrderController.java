package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.Book;
import com.example.bookstore.domain.entity.Order;
import com.example.bookstore.domain.entity.OrderItem;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.web.dto.OrderSummaryResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/orders")
@PreAuthorize("hasAnyRole('STAFF','MANAGER')")
public class StaffOrderController {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    public StaffOrderController(OrderRepository orderRepository, BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping("/pending")
    public List<OrderSummaryResponse> listPending() {
        // Paid orders waiting for staff confirmation
        return orderRepository.findByStatus(OrderStatus.PAID).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getOrderedAt(), o.getTotalAmount(), o.getStatus()))
                .toList();
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable("id") Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (o.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Đơn hàng đã được xác nhận");
        }

        // Spec 2.3 requires checking inventory at confirmation time.
        for (OrderItem item : o.getItems()) {
            Book book = bookRepository.findById(item.getBook().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Book not found"));
            if (book.getStockQuantity() < 0) {
                throw new IllegalStateException("Không đủ hàng trong kho!");
            }
        }
        o.setStatus(OrderStatus.SHIPPING);
        orderRepository.save(o);
        return "Đơn hàng đã chuyển sang trạng thái đang giao hàng!";
    }

    @PostMapping("/{id}/cancel-processing")
    public String cancelProcessing(@PathVariable("id") Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        // Keep status unchanged by spec.
        return "Đã hủy xử lý đơn hàng!";
    }
}

