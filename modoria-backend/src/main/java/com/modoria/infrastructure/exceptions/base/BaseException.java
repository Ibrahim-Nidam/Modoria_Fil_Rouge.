package com.modoria.infrastructure.exceptions.base;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all Modoria exceptions.
 * Provides common fields for error handling.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;

    protected BaseException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.args = null;
    }

    protected BaseException(String message, HttpStatus status, String errorCode, Object... args) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
    }

    protected BaseException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.args = null;
    }
}

