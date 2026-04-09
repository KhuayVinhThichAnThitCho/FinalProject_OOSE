package com.example.bookstore.web.rest;

import com.example.bookstore.service.MockPaymentOutcomeStore;
import com.example.bookstore.service.PaymentResultStatus;
import com.example.bookstore.web.dto.MockAuthorizePaymentRequest;
import com.example.bookstore.web.dto.MockAuthorizePaymentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment/mock")
public class MockPaymentController {

    private final MockPaymentOutcomeStore outcomeStore;

    public MockPaymentController(MockPaymentOutcomeStore outcomeStore) {
        this.outcomeStore = outcomeStore;
    }

    @PostMapping("/authorize")
    public MockAuthorizePaymentResponse authorize(@Valid @RequestBody MockAuthorizePaymentRequest req) {
        PaymentResultStatus status = switch (req.result()) {
            case SUCCESS -> PaymentResultStatus.SUCCESS;
            case INSUFFICIENT_FUNDS -> PaymentResultStatus.INSUFFICIENT_FUNDS;
            case MAINTENANCE -> PaymentResultStatus.MAINTENANCE;
            case USER_CANCELLED -> PaymentResultStatus.USER_CANCELLED;
        };

        outcomeStore.setOutcome(req.orderId(), status);
        return new MockAuthorizePaymentResponse(req.orderId(), status, "Mock payment outcome saved");
    }
}

