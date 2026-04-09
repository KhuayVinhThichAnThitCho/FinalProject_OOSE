package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.Order;
import com.example.bookstore.domain.entity.UserAccount;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserAccountRepository;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.web.dto.CheckoutRequest;
import com.example.bookstore.web.dto.CheckoutResponse;
import com.example.bookstore.web.dto.CreateOrderRequest;
import com.example.bookstore.web.dto.CreateOrderResponse;
import com.example.bookstore.web.dto.OrderDetailResponse;
import com.example.bookstore.web.dto.OrderItemDto;
import com.example.bookstore.web.dto.OrderListResponse;
import com.example.bookstore.web.dto.OrderSummaryResponse;
import com.example.bookstore.web.dto.PayOrderRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserAccountRepository userAccountRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository, UserAccountRepository userAccountRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public CreateOrderResponse createPendingOrder(@Valid @RequestBody CreateOrderRequest request) {
        List<OrderService.ItemRequest> items = request.items().stream()
                .map(i -> new OrderService.ItemRequest(i.bookId(), i.quantity()))
                .toList();

        OrderService.ShippingInfo shippingInfo = new OrderService.ShippingInfo(
                request.receiverName(),
                request.receiverPhone(),
                request.shippingAddress()
        );

        OrderService.CreateOrderResult created = orderService.createPendingOrder(
                request.customerId(),
                items,
                shippingInfo,
                request.shippingFee()
        );

        Order o = orderRepository.findById(created.orderId()).orElseThrow();
        List<OrderItemDto> itemDtos = o.getItems().stream()
                .map(i -> new OrderItemDto(i.getBook().getId(), i.getBook().getTitle(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        CreateOrderResponse.ShippingInfo ship = o.getShippingInfo() == null ? null :
                new CreateOrderResponse.ShippingInfo(
                        o.getShippingInfo().getAddress(),
                        o.getShippingInfo().getReceiverName(),
                        o.getShippingInfo().getReceiverPhone(),
                        o.getShippingInfo().getShippingStatus()
                );

        return new CreateOrderResponse(
                o.getId(),
                o.getOrderedAt(),
                o.getShippingFee(),
                o.getTotalAmount(),
                o.getStatus(),
                ship,
                itemDtos
        );
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CheckoutResponse pay(@PathVariable("id") Long orderId, @Valid @RequestBody PayOrderRequest req, Authentication authentication) {
        String username = authentication == null ? "anonymous" : authentication.getName();
        OrderService.CheckoutResult result = orderService.payOrder(orderId, req.paymentMethodCode(), username);
        return new CheckoutResponse(result.orderId(), result.orderStatus(), result.message());
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request, Authentication authentication) {
        String username = authentication == null ? "anonymous" : authentication.getName();

        List<OrderService.ItemRequest> items = request.items().stream()
                .map(i -> new OrderService.ItemRequest(i.bookId(), i.quantity()))
                .toList();

        OrderService.ShippingInfo shippingInfo = new OrderService.ShippingInfo(
                request.receiverName(),
                request.receiverPhone(),
                request.shippingAddress()
        );

        OrderService.CheckoutResult result = orderService.checkout(
                request.customerId(),
                items,
                shippingInfo,
                request.shippingFee(),
                request.paymentMethodCode(),
                username
        );
        return new CheckoutResponse(result.orderId(), result.orderStatus(), result.message());
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<OrderSummaryResponse> listByCustomer(@RequestParam("customerId") Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderedAtDesc(customerId).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getOrderedAt(), o.getTotalAmount(), o.getStatus()))
                .toList();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderListResponse listMyOrders(Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Unauthenticated");
        }

        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (account.getCustomer() == null) {
            throw new IllegalStateException("Customer profile not linked");
        }

        Long customerId = account.getCustomer().getId();
        List<OrderSummaryResponse> orders = orderRepository.findByCustomerIdOrderByOrderedAtDesc(customerId).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getOrderedAt(), o.getTotalAmount(), o.getStatus()))
                .toList();
        if (orders.isEmpty()) {
            return new OrderListResponse(orders, "Bạn chưa có đơn hàng nào trong lịch sử");
        }
        return new OrderListResponse(orders, null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderDetailResponse getDetail(
            @PathVariable("id") Long orderId,
            @RequestParam("customerId") Long customerId
    ) {
        Order o = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        List<OrderItemDto> items = o.getItems().stream()
                .map(i -> new OrderItemDto(
                        i.getBook().getId(),
                        i.getBook().getTitle(),
                        i.getQuantity(),
                        i.getUnitPrice()
                ))
                .toList();

        OrderDetailResponse.ShippingInfo shipping = o.getShippingInfo() == null ? null :
                new OrderDetailResponse.ShippingInfo(
                        o.getShippingInfo().getAddress(),
                        o.getShippingInfo().getReceiverName(),
                        o.getShippingInfo().getReceiverPhone(),
                        o.getShippingInfo().getShippingStatus()
                );

        return new OrderDetailResponse(
                o.getId(),
                o.getOrderedAt(),
                o.getTotalAmount(),
                o.getStatus(),
                shipping,
                items
        );
    }
}

