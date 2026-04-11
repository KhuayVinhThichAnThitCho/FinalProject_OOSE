package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.CancelRequest;
import com.example.bookstore.domain.enums.CancelRequestStatus;
import com.example.bookstore.domain.entity.Order;
import com.example.bookstore.service.CancelRequestService;
import com.example.bookstore.repository.CancelRequestRepository;
import com.example.bookstore.web.dto.CancelRequestCreate;
import com.example.bookstore.web.dto.CancelRequestDetailResponse;
import com.example.bookstore.web.dto.OrderDetailView;
import com.example.bookstore.web.dto.OrderItemDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cancel-requests")
public class CancelRequestController {

    private final CancelRequestService cancelRequestService;
    private final CancelRequestRepository cancelRequestRepository;

    public CancelRequestController(CancelRequestService cancelRequestService, CancelRequestRepository cancelRequestRepository) {
        this.cancelRequestService = cancelRequestService;
        this.cancelRequestRepository = cancelRequestRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Long create(@Valid @RequestBody CancelRequestCreate req) {
        CancelRequest created = cancelRequestService.create(req.orderId(), req.reason());
        return created.getId();
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER')")
    public List<CancelRequest> viewCancelRequest(@RequestParam(value = "status", required = false) CancelRequestStatus status) {
        if (status == null) {
            return cancelRequestRepository.findAll();
        }
        return cancelRequestRepository.findByStatus(status);
    }

    @GetMapping("/staff/{id}")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER')")
    public CancelRequestDetailResponse getCancelRequest(@PathVariable("id") Long id) {
        CancelRequest req = cancelRequestService.viewCancelRequest(id).getCancelRequest();
        Order o = req.getOrder();

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

        OrderDetailView orderDetailView = new OrderDetailView(
                o.getId(),
                o.getOrderedAt(),
                o.getTotalAmount(),
                o.getOrderStatus(),
                shipping,
                items
        );

        return new CancelRequestDetailResponse(
                req.getId(),
                req.getStatus(),
                req.getReason(),
                req.getRequestedAt(),
                orderDetailView
        );
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER')")
    public String confirmCancelRequest(@PathVariable("id") Long id) {
        return cancelRequestService.confirmCancelRequest(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER')")
    public String rejectCancelRequest(@PathVariable("id") Long id) {
        return cancelRequestService.rejectCancelRequest(id);
    }
}

