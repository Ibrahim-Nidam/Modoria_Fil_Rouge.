package com.modoria.domain.payment.controller;

import com.modoria.domain.payment.dto.response.PaymentIntentResponse;
import com.modoria.domain.payment.service.PaymentService;
import com.modoria.infrastructure.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment endpoints.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment API")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders/{orderId}/create-intent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create payment intent for an order")
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> createPaymentIntent(@PathVariable Long orderId) {
        log.info("Request to create payment intent for order: {}", orderId);
        PaymentIntentResponse response = paymentService.createPaymentIntent(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment intent created successfully"));
    }

    @PostMapping("/confirm/{paymentIntentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Confirm a payment (simulate successful payment)")
    public ResponseEntity<ApiResponse<Void>> confirmPayment(@PathVariable String paymentIntentId) {
        paymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment confirmed successfully"));
    }

    @PostMapping("/orders/{orderId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund a payment (Admin)")
    public ResponseEntity<ApiResponse<Void>> refundPayment(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "Customer request") String reason) {
        paymentService.refundPayment(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment refunded successfully"));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Payment webhook endpoint (for payment providers)")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        log.info("Received payment webhook");
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
