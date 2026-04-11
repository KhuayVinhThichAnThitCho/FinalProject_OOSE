package com.example.bookstore.service;

import com.example.bookstore.domain.entity.*;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.domain.enums.PaymentStatus;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CustomerRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.PaymentMethodRepository;
import com.example.bookstore.repository.PaymentRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            BookRepository bookRepository,
            PaymentMethodRepository paymentMethodRepository,
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.bookRepository = bookRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public CreateOrderResult makeNewOrder(
            Long customerId
    ) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Order order = Order.makeNewOrder(customer);
        Order saved = orderRepository.save(order);
        return new CreateOrderResult(saved.getId(), saved.getStatus(), saved.getTotalAmount(), saved.getShippingFee());
    }

    @Transactional
    public CreateOrderResult confirmOrder(
            Long orderId,
            List<ItemRequest> items,
            ShippingInfo shippingInfo,
            Long shippingFee
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        Map<Long, Integer> quantities = new HashMap<>();
        for (ItemRequest item : items) {
            quantities.merge(item.bookId(), item.quantity(), Integer::sum);
        }

        List<Book> books = bookRepository.findAllById(quantities.keySet());
        order.confirmOrder(quantities, books, shippingFee);
        order.attachShipping(shippingInfo.receiverName(), shippingInfo.receiverPhone(), shippingInfo.address());

        Order saved = orderRepository.save(order);
        return new CreateOrderResult(saved.getId(), saved.getStatus(), saved.getTotalAmount(), saved.getShippingFee());
    }

    @Transactional
    public CheckoutResult requestPayment(Long orderId, String paymentMethodCode, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.isPayable()) {
            throw new IllegalStateException("Order is not in a payable status");
        }

        PaymentMethod method = paymentMethodRepository.findByCodeIgnoreCase(normalizeMethodCode(paymentMethodCode))
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment method"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(method);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);

        PaymentResult result = paymentGateway.processPayment(
                new PaymentRequest(order.getId(), order.getTotalAmount(), paymentMethodCode, username)
        );

        switch (result.status()) {
            case SUCCESS -> {
                order.markPaid(result.partnerTransactionId());
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPartnerTransactionId(result.partnerTransactionId());

                try {
                    for (OrderItem item : order.getItems()) {
                        item.getBook().deductStock(item.getQuantity());
                        bookRepository.save(item.getBook());
                    }
                } catch (OptimisticLockException ex) {
                    throw new IllegalStateException("Inventory was modified concurrently, please retry");
                }

                orderRepository.save(order);
                paymentRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getStatus(), "Đặt hàng và Thanh toán thành công");
            }
            case INSUFFICIENT_FUNDS -> {
                order.markPaymentFailed();
                payment.setStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                paymentRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getStatus(), "Số dư tài khoản không đủ để thực hiện thanh toán");
            }
            case MAINTENANCE -> {
                order.markPaymentFailed();
                payment.setStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                paymentRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getStatus(), "Hệ thống thanh toán đang bảo trì, vui lòng thử lại sau");
            }
            case USER_CANCELLED -> {
                order.markPaymentFailed();
                payment.setStatus(PaymentStatus.CANCELLED);
                orderRepository.save(order);
                paymentRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getStatus(), "Bạn đã hủy đặt hàng");
            }
        }
        throw new IllegalStateException("Trạng thái thanh toán không hỗ trợ");
    }

    @Transactional
    public CheckoutResult checkout(Long orderId, String paymentMethodCode, String username) {
        return requestPayment(orderId, paymentMethodCode, username);
    }

    @Transactional
    public CheckoutResult cancel(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return new CheckoutResult(order.getId(), order.getStatus(), "Đơn hàng đã được hủy trước đó");
        }

        order.cancel();
        orderRepository.save(order);
        return new CheckoutResult(order.getId(), order.getStatus(), "Hủy đơn hàng thành công");
    }

    @Transactional
    public CheckoutResult startShipping(Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        o.startShipping();
        orderRepository.save(o);
        return new CheckoutResult(o.getId(), o.getStatus(), "Đơn hàng đã chuyển sang trạng thái đang giao hàng!");
    }

    @Transactional
    public CheckoutResult confirmOrder(Long orderId) {
        return startShipping(orderId);
    }

    public record ItemRequest(Long bookId, Integer quantity) {}

    public record ShippingInfo(String receiverName, String receiverPhone, String address) {}

    public record CheckoutResult(Long orderId, OrderStatus orderStatus, String message) {}

    public record CreateOrderResult(Long orderId, OrderStatus orderStatus, Long totalAmount, Long shippingFee) {}

    private String normalizeMethodCode(String paymentMethodCode) {
        if (paymentMethodCode == null) return "";
        String upper = paymentMethodCode.toUpperCase();
        int idx = upper.indexOf('_');
        return idx > 0 ? upper.substring(0, idx) : upper;
    }
}

