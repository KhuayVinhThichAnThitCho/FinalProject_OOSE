package com.example.bookstore.web.rest;

import com.example.bookstore.service.PricingService;
import com.example.bookstore.web.dto.UpdateBookPriceRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/books")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerBookController {

    private final PricingService pricingService;

    public ManagerBookController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PutMapping("/{id}/price")
    public PricingService.PricingResult updatePrice(
            @PathVariable("id") Long sachId,
            @Valid @RequestBody UpdateBookPriceRequest req
    ) {
        return pricingService.updatePrice(sachId, req.giaBanMoi(), req.thoiGianApDung(), req.chapNhanBanLo());
    }
}

