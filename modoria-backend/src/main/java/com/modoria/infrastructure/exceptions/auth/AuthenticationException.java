package com.modoria.infrastructure.exceptions.auth;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTH_001");
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, "AUTH_001");
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid email or password");
    }

    public static AuthenticationException accountDisabled() {
        return new AuthenticationException("Account is disabled");
    }

    public static AuthenticationException accountLocked() {
        return new AuthenticationException("Account is locked due to too many failed login attempts");
    }
}


