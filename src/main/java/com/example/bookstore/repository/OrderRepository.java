package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.Order;
import com.example.bookstore.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByOrderedAtDesc(Long customerId);
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByOrderedAtBetween(Instant from, Instant to);

    default List<Order> findOrdersByCustomer(Long customerId) {
        return findByCustomerIdOrderByOrderedAtDesc(customerId);
    }

    default Optional<Order> findOrder(Long orderId, Long customerId) {
        return findByIdAndCustomerId(orderId, customerId);
    }
}

