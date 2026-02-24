package com.modoria.infrastructure.exceptions.resource;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to create a resource that already exists.
 */
public class ResourceAlreadyExistsException extends BaseException {

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "RES_002");
    }

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT, "RES_002", resourceName, fieldName, fieldValue);
    }

    public static ResourceAlreadyExistsException user(String email) {
        return new ResourceAlreadyExistsException("User", "email", email);
    }

    public static ResourceAlreadyExistsException product(String sku) {
        return new ResourceAlreadyExistsException("Product", "sku", sku);
    }

    public static ResourceAlreadyExistsException category(String slug) {
        return new ResourceAlreadyExistsException("Category", "slug", slug);
    }

    public static ResourceAlreadyExistsException role(String name) {
        return new ResourceAlreadyExistsException("Role", "name", name);
    }

    public static ResourceAlreadyExistsException review(Long userId, Long productId) {
        return new ResourceAlreadyExistsException(
                String.format("Review already exists for user %d on product %d", userId, productId));
    }
}


