package com.modoria.domain.cart.service;

import com.modoria.domain.cart.dto.request.AddToCartRequest;
import com.modoria.domain.cart.dto.request.UpdateCartItemRequest;
import com.modoria.domain.cart.dto.response.CartResponse;

/**
 * Service interface for cart operations.
 */
public interface CartService {

    CartResponse getCart();

    CartResponse getCartByUserId(Long userId);

    CartResponse addItem(AddToCartRequest request);

    CartResponse updateItem(Long productId, UpdateCartItemRequest request);

    CartResponse removeItem(Long productId, Long variantId);

    CartResponse clearCart();

    int getCartItemCount();
}
