package com.modoria.infrastructure.exceptions.business;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when operations on an empty cart are attempted.
 */
public class CartEmptyException extends BaseException {

    public CartEmptyException() {
        super("Cart is empty", HttpStatus.BAD_REQUEST, "BUS_004");
    }

    public CartEmptyException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUS_004");
    }

    public static CartEmptyException cannotCheckout() {
        return new CartEmptyException("Cannot checkout with an empty cart");
    }
}


