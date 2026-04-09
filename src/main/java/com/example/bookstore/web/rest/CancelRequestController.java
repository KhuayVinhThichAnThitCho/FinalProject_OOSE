package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.YeuCauHuyDon;
import com.example.bookstore.service.CancelRequestService;
import com.example.bookstore.web.dto.CancelRequestCreate;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cancel-requests")
public class CancelRequestController {

    private final CancelRequestService cancelRequestService;

    public CancelRequestController(CancelRequestService cancelRequestService) {
        this.cancelRequestService = cancelRequestService;
    }

    @PostMapping
    public Long create(@Valid @RequestBody CancelRequestCreate req) {
        YeuCauHuyDon created = cancelRequestService.create(req.donHangId(), req.lyDoHuy());
        return created.getId();
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable("id") Long id) {
        return cancelRequestService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable("id") Long id) {
        return cancelRequestService.reject(id);
    }
}

