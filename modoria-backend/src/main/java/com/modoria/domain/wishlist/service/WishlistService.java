package com.modoria.domain.wishlist.service;

import com.modoria.domain.wishlist.dto.request.AddToWishlistRequest;
import com.modoria.domain.wishlist.dto.response.WishlistItemResponse;

import java.util.List;

public interface WishlistService {

    WishlistItemResponse addToWishlist(AddToWishlistRequest request);

    List<WishlistItemResponse> getCurrentUserWishlist();

    void removeFromWishlist(Long itemId);

    void moveToCart(Long itemId);
}
