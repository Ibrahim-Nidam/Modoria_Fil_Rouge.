package com.modoria.domain.payment.entity;

import com.modoria.domain.base.BaseEntity;
import com.modoria.domain.order.entity.Order;
import com.modoria.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity representing a payment transaction.
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_id"),
        @Index(name = "idx_payment_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "payment_provider", length = 50)
    private String paymentProvider; // STRIPE, PAYPAL, etc.

    @Column(name = "payment_intent_id", length = 255)
    private String paymentIntentId;

    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "MAD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON for additional provider-specific data
}
