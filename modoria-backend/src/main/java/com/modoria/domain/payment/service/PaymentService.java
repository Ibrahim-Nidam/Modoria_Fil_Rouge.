package com.modoria.domain.payment.service;

import com.modoria.domain.payment.dto.response.PaymentIntentResponse;

/**
 * Service interface for payment operations.
 */
public interface PaymentService {

    PaymentIntentResponse createPaymentIntent(Long orderId);

    void handleWebhook(String payload, String signature);

    void confirmPayment(String paymentIntentId);

    void refundPayment(Long orderId, String reason);
}




