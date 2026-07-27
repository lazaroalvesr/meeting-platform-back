package com.project.meeting_platform.auth.controller.Payment;

import com.project.meeting_platform.auth.Service.Payment.PaymentService;
import com.project.meeting_platform.auth.dto.Payment.CreatePaymentRequest;
import com.project.meeting_platform.auth.dto.Payment.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.create(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(paymentService.list(authentication.getName()));
    }

    @PatchMapping("/{paymentId}/mark-paid")
    public ResponseEntity<PaymentResponse> markAsPaid(
            @PathVariable java.util.UUID paymentId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(paymentService.markAsPaid(authentication.getName(), paymentId));
    }

}
