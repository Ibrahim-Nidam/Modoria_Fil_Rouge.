package com.modoria.domain.cart.mapper;

import com.modoria.domain.cart.entity.Cart;
import com.modoria.domain.cart.entity.CartItem;
import com.modoria.domain.cart.dto.response.CartItemResponse;
import com.modoria.domain.cart.dto.response.CartResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Simple mapper for Cart entity.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CartMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "totalItems", ignore = true)
    @Mapping(target = "uniqueItemCount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    CartResponse toResponse(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "productImageUrl", ignore = true)
    @Mapping(target = "unitPrice", source = "product.price")
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "inStock", ignore = true)
    @Mapping(target = "availableQuantity", source = "product.quantity")
    CartItemResponse toItemResponse(CartItem cartItem);

    List<CartItemResponse> toItemResponseList(List<CartItem> items);

    @AfterMapping
    default void calculateCartFields(@MappingTarget CartResponse response, Cart cart) {
        response.setTotalItems(cart.getTotalItems());
        response.setUniqueItemCount(cart.getUniqueItemCount());
        response.setTotalAmount(cart.getTotalAmount());
    }

    @AfterMapping
    default void calculateItemFields(@MappingTarget CartItemResponse response, CartItem cartItem) {
        if (cartItem.getProduct() != null) {
            response.setInStock(cartItem.getProduct().isInStock());
            if (cartItem.getProduct().getPrimaryImage() != null) {
                response.setProductImageUrl(cartItem.getProduct().getPrimaryImage().getUrl());
            }
        }
        response.setSubtotal(cartItem.getSubtotal());
    }
}


