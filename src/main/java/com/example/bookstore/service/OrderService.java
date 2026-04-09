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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        order.setCustomer(customer);
        order.setOrderDate(Instant.now());
        order.setStatus(OrderStatus.PENDING);

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

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            throw new IllegalStateException("Order was already confirmed");
        }

        Map<Long, Integer> quantities = new HashMap<>();
        for (ItemRequest item : items) {
            quantities.merge(item.bookId(), item.quantity(), Integer::sum);
        }

        List<Book> books = bookRepository.findAllById(quantities.keySet());
        if (books.size() != quantities.size()) {
            throw new IllegalArgumentException("Book not found");
        }

        long sf = shippingFee == null ? 0L : shippingFee;
        if (sf < 0) {
            throw new IllegalArgumentException("Invalid shipping fee");
        }

        long subtotal = 0L;
        List<OrderItem> orderItems = new ArrayList<>();
        for (Book book : books) {
            int qty = quantities.get(book.getId());
            if (book.getStockQuantity() < qty) {
                throw new IllegalStateException("Insufficient stock for book: " + book.getTitle());
            }

            subtotal += book.getPrice() * (long) qty;

            OrderItem line = new OrderItem();
            line.setOrder(order);
            line.setBook(book);
            line.setQuantity(qty);
            line.setUnitPrice(book.getPrice());
            orderItems.add(line);
        }
        order.getItems().addAll(orderItems);

        com.example.bookstore.domain.entity.ShippingInfo si = new com.example.bookstore.domain.entity.ShippingInfo();
        si.setOrder(order);
        si.setReceiverName(shippingInfo.receiverName());
        si.setReceiverPhone(shippingInfo.receiverPhone());
        si.setAddress(shippingInfo.address());
        si.setShippingStatus("PENDING");
        order.setShippingInfo(si);

        order.setShippingFee(sf);
        order.setTotalAmount(subtotal + sf);

        order.setStatus(OrderStatus.PROCESSING);

        Order saved = orderRepository.save(order);
        return new CreateOrderResult(saved.getId(), saved.getStatus(), saved.getTotalAmount(), saved.getShippingFee());
    }

    @Transactional
    public CheckoutResult requestPayment(Long orderId, String paymentMethodCode, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
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
                order.setStatus(OrderStatus.PAID);
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPartnerTransactionId(result.partnerTransactionId());

                try {
                    for (OrderItem item : order.getItems()) {
                        Book book = bookRepository.findById(item.getBook().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
                        if (book.getStockQuantity() < item.getQuantity()) {
                            throw new IllegalStateException("Không đủ hàng trong kho!");
                        }
                        book.setStockQuantity(book.getStockQuantity() - item.getQuantity());
                        bookRepository.save(book);
                    }
                } catch (OptimisticLockException ex) {
                    throw new IllegalStateException("Inventory was modified concurrently, please retry");
                }

                orderRepository.save(order);
                paymentRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getStatus(), "Đặt hàng và Thanh toán thành công");
            }
            case INSUFFICIENT_FUNDS -> {
                order.setStatus(OrderStatus.PENDING);
                payment.setStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                paymentRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getStatus(), "Số dư tài khoản không đủ để thực hiện thanh toán");
            }
            case MAINTENANCE -> {
                order.setStatus(OrderStatus.PENDING);
                payment.setStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                paymentRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getStatus(), "Hệ thống thanh toán đang bảo trì, vui lòng thử lại sau");
            }
            case USER_CANCELLED -> {
                order.setStatus(OrderStatus.PENDING);
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
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Không thể hủy đơn hàng đã giao");
        }
        if (order.getStatus() == OrderStatus.SHIPPING) {
            throw new IllegalStateException("Không thể hủy đơn hàng đang giao");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return new CheckoutResult(order.getId(), order.getStatus(), "Hủy đơn hàng thành công");
    }

    @Transactional
    public CheckoutResult startShipping(Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (o.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái PAID để xác nhận");
        }

        for (OrderItem item : o.getItems()) {
            Book book = bookRepository.findById(item.getBook().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Book not found"));
            if (book.getStockQuantity() < 0) {
                throw new IllegalStateException("Không đủ hàng trong kho!");
            }
        }

        o.setStatus(OrderStatus.SHIPPING);
        orderRepository.save(o);
        return new CheckoutResult(o.getId(), o.getStatus(), "Đơn hàng đã chuyển sang trạng thái đang giao hàng!");
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

