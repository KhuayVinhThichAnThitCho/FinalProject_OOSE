package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.CancellationRequest;
import com.example.bookstore.domain.enums.CancelRequestStatus;
import com.example.bookstore.service.CancelRequestService;
import com.example.bookstore.repository.CancellationRequestRepository;
import com.example.bookstore.web.dto.CancelRequestCreate;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cancel-requests")
public class CancelRequestController {

    private final CancelRequestService cancelRequestService;
    private final CancellationRequestRepository cancellationRequestRepository;

    public CancelRequestController(CancelRequestService cancelRequestService, CancellationRequestRepository cancellationRequestRepository) {
        this.cancelRequestService = cancelRequestService;
        this.cancellationRequestRepository = cancellationRequestRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Long create(@Valid @RequestBody CancelRequestCreate req) {
        CancellationRequest created = cancelRequestService.create(req.orderId(), req.reason());
        return created.getId();
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER')")
    public List<CancellationRequest> listForStaff(@RequestParam(value = "status", required = false) CancelRequestStatus status) {
        if (status == null) {
            return cancellationRequestRepository.findAll();
        }
        return cancellationRequestRepository.findByStatus(status);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER')")
    public String approve(@PathVariable("id") Long id) {
        return cancelRequestService.approve(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER')")
    public String reject(@PathVariable("id") Long id) {
        return cancelRequestService.reject(id);
    }
}

