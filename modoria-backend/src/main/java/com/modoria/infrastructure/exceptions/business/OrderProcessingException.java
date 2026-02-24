package com.modoria.infrastructure.exceptions.business;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when order processing fails.
 */
public class OrderProcessingException extends BaseException {

    public OrderProcessingException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUS_002");
    }

    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "BUS_002");
    }

    public static OrderProcessingException cannotCancel(String orderNumber) {
        return new OrderProcessingException(
                String.format("Order %s cannot be cancelled in its current state", orderNumber));
    }

    public static OrderProcessingException invalidStatusTransition(String from, String to) {
        return new OrderProcessingException(
                String.format("Cannot transition order from %s to %s", from, to));
    }

    public static OrderProcessingException alreadyPaid(String orderNumber) {
        return new OrderProcessingException(
                String.format("Order %s has already been paid", orderNumber));
    }
}


