package com.modoria.infrastructure.exceptions.resource;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's a conflict with the current state of a
 * resource.
 */
public class ResourceConflictException extends BaseException {

    public ResourceConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "RES_003");
    }

    public static ResourceConflictException optimisticLock(String resourceName) {
        return new ResourceConflictException(
                String.format("%s was modified by another user. Please refresh and try again.", resourceName));
    }

    public static ResourceConflictException stateConflict(String message) {
        return new ResourceConflictException(message);
    }
}


