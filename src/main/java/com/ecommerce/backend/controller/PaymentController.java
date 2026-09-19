package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.PaymentDTO;
import com.ecommerce.backend.dto.PaymentResponseDTO;
import com.ecommerce.backend.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponseDTO makePayment(
            @Valid @RequestBody PaymentDTO paymentDTO,
            Authentication authentication) {

        String email = authentication.getName();

        return paymentService.makePayment(
                email,
                paymentDTO
        );
    }

    @GetMapping
    public List<PaymentResponseDTO> getMyPayments(
            Authentication authentication) {

        String email = authentication.getName();

        return paymentService.getMyPayments(email);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponseDTO getPayment(
            @PathVariable Long paymentId,
            Authentication authentication) {

        String email = authentication.getName();

        return paymentService.getPayment(
                email,
                paymentId
        );
    }
}