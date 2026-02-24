package com.modoria.domain.wishlist.controller;

import com.modoria.domain.wishlist.dto.request.AddToWishlistRequest;
import com.modoria.domain.wishlist.dto.response.WishlistItemResponse;
import com.modoria.domain.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "User wishlist management")
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    @Operation(summary = "Add item to wishlist")
    public ResponseEntity<WishlistItemResponse> addToWishlist(@Valid @RequestBody AddToWishlistRequest request) {
        log.info("Request to add product {} to wishlist", request.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.addToWishlist(request));
    }

    @GetMapping
    @Operation(summary = "Get current user's wishlist")
    public ResponseEntity<List<WishlistItemResponse>> getCurrentUserWishlist() {
        return ResponseEntity.ok(wishlistService.getCurrentUserWishlist());
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Remove item from wishlist")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromWishlist(@PathVariable Long itemId) {
        wishlistService.removeFromWishlist(itemId);
    }

    @PostMapping("/{itemId}/move-to-cart")
    @Operation(summary = "Move item from wishlist to cart")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveToCart(@PathVariable Long itemId) {
        wishlistService.moveToCart(itemId);
    }
}
