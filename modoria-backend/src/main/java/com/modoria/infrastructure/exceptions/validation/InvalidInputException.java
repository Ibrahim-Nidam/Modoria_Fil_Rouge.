package com.modoria.infrastructure.exceptions.validation;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when input data is invalid.
 */
public class InvalidInputException extends BaseException {

    public InvalidInputException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VAL_002");
    }

    public static InvalidInputException invalidFormat(String field, String expectedFormat) {
        return new InvalidInputException(
                String.format("Invalid format for %s. Expected: %s", field, expectedFormat));
    }

    public static InvalidInputException outOfRange(String field, Object min, Object max) {
        return new InvalidInputException(
                String.format("%s must be between %s and %s", field, min, max));
    }

    public static InvalidInputException required(String field) {
        return new InvalidInputException(String.format("%s is required", field));
    }
}


