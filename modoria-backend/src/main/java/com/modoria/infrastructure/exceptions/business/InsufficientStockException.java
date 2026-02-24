package com.modoria.infrastructure.exceptions.business;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is insufficient stock for an operation.
 */
public class InsufficientStockException extends BaseException {

    public InsufficientStockException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUS_001");
    }

    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format("Insufficient stock for '%s'. Requested: %d, Available: %d",
                productName, requested, available),
                HttpStatus.BAD_REQUEST, "BUS_001", productName, requested, available);
    }

    public static InsufficientStockException outOfStock(String productName) {
        return new InsufficientStockException(String.format("'%s' is out of stock", productName));
    }

    public static InsufficientStockException notEnough(String productName, int requested, int available) {
        return new InsufficientStockException(productName, requested, available);
    }
}


