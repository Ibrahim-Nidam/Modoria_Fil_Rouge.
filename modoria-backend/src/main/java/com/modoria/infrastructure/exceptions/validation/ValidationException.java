package com.modoria.infrastructure.exceptions.validation;

import com.modoria.infrastructure.exceptions.base.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when input validation fails.
 */
@Getter
public class ValidationException extends BaseException {

    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VAL_001");
        this.errors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message, HttpStatus.BAD_REQUEST, "VAL_001");
        this.errors = errors;
    }

    public ValidationException(String field, String error) {
        super("Validation failed", HttpStatus.BAD_REQUEST, "VAL_001");
        this.errors = new HashMap<>();
        this.errors.put(field, error);
    }

    public static ValidationException of(String field, String error) {
        return new ValidationException(field, error);
    }

    public static ValidationException of(Map<String, String> errors) {
        return new ValidationException("Validation failed", errors);
    }
}


