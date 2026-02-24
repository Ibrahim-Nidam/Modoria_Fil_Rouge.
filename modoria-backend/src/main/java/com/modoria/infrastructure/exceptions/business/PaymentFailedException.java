package com.modoria.infrastructure.exceptions.business;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when payment processing fails.
 */
public class PaymentFailedException extends BaseException {

    public PaymentFailedException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED, "BUS_003");
    }

    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause, HttpStatus.PAYMENT_REQUIRED, "BUS_003");
    }

    public static PaymentFailedException cardDeclined() {
        return new PaymentFailedException("Payment card was declined");
    }

    public static PaymentFailedException insufficientFunds() {
        return new PaymentFailedException("Insufficient funds");
    }

    public static PaymentFailedException processingError() {
        return new PaymentFailedException("Payment processing error. Please try again.");
    }

    public static PaymentFailedException invalidPaymentMethod() {
        return new PaymentFailedException("Invalid payment method");
    }

    public static PaymentFailedException stripeError(String message) {
        return new PaymentFailedException("Stripe error: " + message);
    }
}


