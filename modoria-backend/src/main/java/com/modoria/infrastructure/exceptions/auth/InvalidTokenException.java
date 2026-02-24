package com.modoria.infrastructure.exceptions.auth;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a JWT token is invalid or expired.
 */
public class InvalidTokenException extends BaseException {

    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTH_002");
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, "AUTH_002");
    }

    public static InvalidTokenException expired() {
        return new InvalidTokenException("Token has expired");
    }

    public static InvalidTokenException malformed() {
        return new InvalidTokenException("Invalid token format");
    }

    public static InvalidTokenException unsupported() {
        return new InvalidTokenException("Unsupported token type");
    }

    public static InvalidTokenException signatureInvalid() {
        return new InvalidTokenException("Token signature is invalid");
    }
}


