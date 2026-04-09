package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.Order;
import com.example.bookstore.domain.entity.UserAccount;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserAccountRepository;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.web.dto.CheckoutOrderRequest;
import com.example.bookstore.web.dto.CheckoutResponse;
import com.example.bookstore.web.dto.ConfirmOrderRequest;
import com.example.bookstore.web.dto.CreateOrderResponse;
import com.example.bookstore.web.dto.MakeNewOrderRequest;
import com.example.bookstore.web.dto.OrderDetailResponse;
import com.example.bookstore.web.dto.OrderItemDto;
import com.example.bookstore.web.dto.OrderListResponse;
import com.example.bookstore.web.dto.OrderSummaryResponse;
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
    public CreateOrderResponse makeNewOrder(@Valid @RequestBody MakeNewOrderRequest request) {
        OrderService.CreateOrderResult created = orderService.makeNewOrder(request.customerId());

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

    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CheckoutResponse checkout(@PathVariable("id") Long orderId, @Valid @RequestBody CheckoutOrderRequest req, Authentication authentication) {
        String username = authentication == null ? "anonymous" : authentication.getName();
        OrderService.CheckoutResult result = orderService.checkout(orderId, req.paymentMethodCode(), username);
        return new CheckoutResponse(result.orderId(), result.orderStatus(), result.message());
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CreateOrderResponse confirmOrder(@PathVariable("id") Long orderId, @Valid @RequestBody ConfirmOrderRequest request) {
        List<OrderService.ItemRequest> items = request.items().stream()
                .map(i -> new OrderService.ItemRequest(i.bookId(), i.quantity()))
                .toList();

        OrderService.ShippingInfo shippingInfo = new OrderService.ShippingInfo(
                request.receiverName(),
                request.receiverPhone(),
                request.shippingAddress()
        );

        OrderService.CreateOrderResult created = orderService.confirmOrder(
                orderId,
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

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CheckoutResponse cancel(@PathVariable("id") Long orderId, @RequestParam("customerId") Long customerId) {
        OrderService.CheckoutResult result = orderService.cancel(orderId, customerId);
        return new CheckoutResponse(result.orderId(), result.orderStatus(), result.message());
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<OrderSummaryResponse> viewOrders(@RequestParam("customerId") Long customerId) {
        return orderRepository.findOrdersByCustomer(customerId).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getOrderedAt(), o.getTotalAmount(), o.getStatus()))
                .toList();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderListResponse viewOrders(Authentication authentication) {
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
        List<OrderSummaryResponse> orders = orderRepository.findOrdersByCustomer(customerId).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getOrderedAt(), o.getTotalAmount(), o.getStatus()))
                .toList();
        if (orders.isEmpty()) {
            return new OrderListResponse(orders, "Bạn chưa có đơn hàng nào trong lịch sử");
        }
        return new OrderListResponse(orders, null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderDetailResponse viewOrderDetail(
            @PathVariable("id") Long orderId,
            @RequestParam("customerId") Long customerId
    ) {
        Order o = orderRepository.findOrder(orderId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin đơn hàng."));

        List<OrderItemDto> items = o.getOrderDetails().stream()
                .map(i -> new OrderItemDto(
                        i.getBookInfo().getId(),
                        i.getBookInfo().getTitle(),
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

