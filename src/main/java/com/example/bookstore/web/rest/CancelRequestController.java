package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.YeuCauHuyDon;
import com.example.bookstore.domain.enums.CancelRequestStatus;
import com.example.bookstore.service.CancelRequestService;
import com.example.bookstore.repository.YeuCauHuyDonRepository;
import com.example.bookstore.web.dto.CancelRequestCreate;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cancel-requests")
public class CancelRequestController {

    private final CancelRequestService cancelRequestService;
    private final YeuCauHuyDonRepository yeuCauHuyDonRepository;

    public CancelRequestController(CancelRequestService cancelRequestService, YeuCauHuyDonRepository yeuCauHuyDonRepository) {
        this.cancelRequestService = cancelRequestService;
        this.yeuCauHuyDonRepository = yeuCauHuyDonRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Long create(@Valid @RequestBody CancelRequestCreate req) {
        YeuCauHuyDon created = cancelRequestService.create(req.donHangId(), req.lyDoHuy());
        return created.getId();
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER')")
    public List<YeuCauHuyDon> listForStaff(@RequestParam(value = "status", required = false) CancelRequestStatus status) {
        if (status == null) {
            return yeuCauHuyDonRepository.findAll();
        }
        return yeuCauHuyDonRepository.findByTrangThaiYeuCau(status);
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

