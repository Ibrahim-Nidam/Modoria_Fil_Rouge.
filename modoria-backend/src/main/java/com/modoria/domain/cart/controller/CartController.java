package com.modoria.domain.cart.controller;

import com.modoria.domain.cart.dto.request.AddToCartRequest;
import com.modoria.domain.cart.dto.request.UpdateCartItemRequest;
import com.modoria.domain.cart.dto.response.CartResponse;
import com.modoria.infrastructure.common.dto.ApiResponse;
import com.modoria.domain.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for shopping cart endpoints.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping Cart API")
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        CartResponse response = cartService.getCart();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody AddToCartRequest request) {
        log.info("Request to add item to cart - Product: {}", request.getProductId());
        CartResponse response = cartService.addItem(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Item added to cart successfully"));
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateItem(productId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart item updated successfully"));
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId) {
        CartResponse response = cartService.removeItem(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success(response, "Item removed from cart successfully"));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart() {
        log.info("Request to clear cart");
        CartResponse response = cartService.clearCart();
        return ResponseEntity.ok(ApiResponse.success(response, "Cart cleared successfully"));
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get cart item count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount() {
        int count = cartService.getCartItemCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
