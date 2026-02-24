package com.modoria.infrastructure.exceptions.resource;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RES_001");
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND, "RES_001", resourceName, fieldName, fieldValue);
    }

    public static ResourceNotFoundException user(Long id) {
        return new ResourceNotFoundException("User", "id", id);
    }

    public static ResourceNotFoundException user(String email) {
        return new ResourceNotFoundException("User", "email", email);
    }

    public static ResourceNotFoundException product(Long id) {
        return new ResourceNotFoundException("Product", "id", id);
    }

    public static ResourceNotFoundException product(String slug) {
        return new ResourceNotFoundException("Product", "slug", slug);
    }

    public static ResourceNotFoundException category(Long id) {
        return new ResourceNotFoundException("Category", "id", id);
    }

    public static ResourceNotFoundException order(Long id) {
        return new ResourceNotFoundException("Order", "id", id);
    }

    public static ResourceNotFoundException order(String orderNumber) {
        return new ResourceNotFoundException("Order", "orderNumber", orderNumber);
    }

    public static ResourceNotFoundException cart(Long userId) {
        return new ResourceNotFoundException("Cart", "userId", userId);
    }

    public static ResourceNotFoundException review(Long id) {
        return new ResourceNotFoundException("Review", "id", id);
    }

    public static ResourceNotFoundException role(Long id) {
        return new ResourceNotFoundException("Role", "id", id);
    }

    public static ResourceNotFoundException role(String name) {
        return new ResourceNotFoundException("Role", "name", name);
    }

    public static ResourceNotFoundException season(Long id) {
        return new ResourceNotFoundException("Season", "id", id);
    }

    public static ResourceNotFoundException chatRoom(Long id) {
        return new ResourceNotFoundException("ChatRoom", "id", id);
    }
}


