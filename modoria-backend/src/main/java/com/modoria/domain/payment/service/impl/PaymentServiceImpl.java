package com.modoria.domain.payment.service.impl;

import com.modoria.domain.order.entity.Order;
import com.modoria.domain.order.enums.OrderStatus;
import com.modoria.domain.order.repository.OrderRepository;
import com.modoria.domain.payment.dto.response.PaymentIntentResponse;
import com.modoria.domain.payment.entity.Payment;
import com.modoria.domain.payment.enums.PaymentStatus;
import com.modoria.domain.payment.repository.PaymentRepository;
import com.modoria.domain.payment.service.PaymentService;
import com.modoria.infrastructure.exceptions.business.OrderProcessingException;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${modoria.stripe.mode:test}")
    private String stripeMode;

    @Value("${modoria.stripe.webhook-secret:}")
    private String webhookSecret;

    @Override
    public PaymentIntentResponse createPaymentIntent(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Check if payment already exists
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            // Optional: return existing intent if PENDING
            Payment existingPayment = paymentRepository.findByOrderId(orderId).get();
            if (existingPayment.getStatus() == PaymentStatus.PENDING) {
                // In a real app, retrieve client_secret from Stripe or DB
                // For now, throw exception to keep flow simple
                throw new OrderProcessingException("Payment already initiated for this order");
            }
        }

        // Check order status
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderProcessingException("Order is not in PENDING status");
        }

        try {
            // Stripe expects amount in cents (or smallest unit)
            long amountInSmallestUnit = order.getTotalAmount().multiply(new BigDecimal(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInSmallestUnit)
                    .setCurrency(order.getCurrency().toLowerCase())
                    .setDescription("Order #" + order.getOrderNumber())
                    .putMetadata("orderId", order.getId().toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // Create payment record
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentProvider("STRIPE")
                    .paymentIntentId(intent.getId())
                    .amount(order.getTotalAmount())
                    .currency(order.getCurrency())
                    .status(PaymentStatus.PENDING)
                    .build();

            paymentRepository.save(payment);

            // Link payment to order
            order.setPayment(payment);
            orderRepository.save(order);

            return PaymentIntentResponse.builder()
                    .paymentIntentId(intent.getId())
                    .clientSecret(intent.getClientSecret())
                    .amount(order.getTotalAmount())
                    .currency(order.getCurrency())
                    .status(intent.getStatus())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe API error: {}", e.getMessage(), e);
            throw new OrderProcessingException("Failed to create payment intent: " + e.getMessage());
        }
    }

    @Override
    public void handleWebhook(String payload, String signature) {
        try {
            com.stripe.model.Event event = com.stripe.net.Webhook.constructEvent(
                    payload, signature, webhookSecret);

            log.info("Received authenticated Stripe webhook: {}", event.getType());

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                    if (intent != null) {
                        confirmPayment(intent.getId());
                    }
                    break;
                case "payment_intent.payment_failed":
                    log.warn("Payment intent failed for event: {}", event.getId());
                    // Handle failure (e.g., notify user)
                    break;
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }
        } catch (com.stripe.exception.SignatureVerificationException e) {
            log.error("Invalid Stripe signature: {}", e.getMessage());
            throw new OrderProcessingException("Invalid webhook signature");
        }
    }

    @Override
    public void confirmPayment(String paymentIntentId) {
        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentIntentId", paymentIntentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return; // Already confirmed
        }

        try {
            // Verify status with Stripe
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            // In TEST mode, if payment is pending, try to auto-confirm with valid test card
            if ("requires_payment_method".equals(intent.getStatus()) && "test".equalsIgnoreCase(stripeMode)) {
                try {
                    com.stripe.param.PaymentIntentConfirmParams confirmParams = com.stripe.param.PaymentIntentConfirmParams
                            .builder()
                            .setPaymentMethod("pm_card_visa")
                            .setReturnUrl("https://example.com/return")
                            .build();
                    intent = intent.confirm(confirmParams);
                    log.info("Auto-confirmed payment {} with pm_card_visa in TEST mode", paymentIntentId);
                } catch (StripeException e) {
                    log.warn("Auto-confirm failed: {}", e.getMessage());
                    // Continue to check status normally
                }
            }

            if ("succeeded".equals(intent.getStatus())) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                payment.setTransactionId(intent.getLatestCharge()); // Valid if available
                paymentRepository.save(payment);

                // Update order status
                Order order = payment.getOrder();
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                log.info("Payment confirmed for order: {}", order.getOrderNumber());
            } else {
                log.warn("Attempt to confirm payment {} but status is {}", paymentIntentId, intent.getStatus());
                throw new OrderProcessingException("Payment not successful. Status: " + intent.getStatus());
            }

        } catch (StripeException e) {
            log.error("Stripe API error during confirmation: {}", e.getMessage(), e);
            throw new OrderProcessingException("Failed to verify payment: " + e.getMessage());
        }
    }

    @Override
    public void refundPayment(Long orderId, String reason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new OrderProcessingException("Can only refund completed payments");
        }

        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getPaymentIntentId())
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER) // or DUPLICATE, FRAUDULENT
                    .build();

            Refund refund = Refund.create(params);

            if ("succeeded".equals(refund.getStatus()) || "pending".equals(refund.getStatus())) {
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(LocalDateTime.now());
                payment.setRefundAmount(payment.getAmount()); // Full refund for simplicity
                payment.setFailureReason(reason);
                paymentRepository.save(payment);

                // Update order status
                Order order = payment.getOrder();
                order.setStatus(OrderStatus.REFUNDED);
                orderRepository.save(order);

                log.info("Payment refunded for order: {}", order.getOrderNumber());
            } else {
                throw new OrderProcessingException("Refund failed: " + refund.getStatus());
            }

        } catch (StripeException e) {
            log.error("Stripe refund error: {}", e.getMessage(), e);
            throw new OrderProcessingException("Failed to process refund: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
    }
}
