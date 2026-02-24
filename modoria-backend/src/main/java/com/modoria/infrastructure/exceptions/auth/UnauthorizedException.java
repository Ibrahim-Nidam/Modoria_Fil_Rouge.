package com.modoria.infrastructure.exceptions.auth;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user lacks required permissions.
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "AUTH_003");
    }

    public static UnauthorizedException accessDenied() {
        return new UnauthorizedException("Access denied");
    }

    public static UnauthorizedException insufficientPermissions() {
        return new UnauthorizedException("Insufficient permissions to perform this action");
    }

    public static UnauthorizedException roleRequired(String role) {
        return new UnauthorizedException("Role " + role + " is required to perform this action");
    }
}


