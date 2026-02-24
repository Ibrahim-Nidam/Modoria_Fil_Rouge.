package com.modoria.domain.wishlist.service.impl;

import com.modoria.domain.cart.entity.Cart;
import com.modoria.domain.cart.entity.CartItem;
import com.modoria.domain.cart.repository.CartRepository;
import com.modoria.domain.product.entity.Product;
import com.modoria.domain.product.entity.ProductVariant;
import com.modoria.domain.product.repository.ProductRepository;
import com.modoria.domain.user.entity.User;
import com.modoria.domain.user.repository.UserRepository;
import com.modoria.domain.wishlist.dto.request.AddToWishlistRequest;
import com.modoria.domain.wishlist.dto.response.WishlistItemResponse;
import com.modoria.domain.wishlist.entity.WishlistItem;
import com.modoria.domain.wishlist.mapper.WishlistMapper;
import com.modoria.domain.wishlist.repository.WishlistRepository;
import com.modoria.domain.wishlist.service.WishlistService;
import com.modoria.infrastructure.exceptions.resource.ResourceAlreadyExistsException;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.infrastructure.exceptions.auth.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WishlistServiceImpl implements WishlistService {

        private final WishlistRepository wishlistRepository;
        private final ProductRepository productRepository;
        private final UserRepository userRepository;
        private final CartRepository cartRepository;
        private final WishlistMapper wishlistMapper;

        @Override
        public WishlistItemResponse addToWishlist(AddToWishlistRequest request) {
                User user = getCurrentUser();
                Product product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id",
                                                request.getProductId()));

                ProductVariant variant = null;
                if (request.getProductVariantId() != null) {
                        variant = product.getVariants().stream()
                                        .filter(v -> v.getId().equals(request.getProductVariantId()))
                                        .findFirst()
                                        .orElseThrow(
                                                        () -> new ResourceNotFoundException("ProductVariant", "id",
                                                                        request.getProductVariantId()));
                }

                // Check if already in wishlist
                Long variantId = variant != null ? variant.getId() : null;
                if (wishlistRepository.existsByUserIdAndProductIdAndProductVariantId(
                                user.getId(), product.getId(), variantId)) {
                        log.debug("User {} tried to add duplicate item to wishlist (product: {})", user.getId(),
                                        product.getId());
                        throw new ResourceAlreadyExistsException("This item is already in your wishlist");
                }

                WishlistItem item = WishlistItem.builder()
                                .user(user)
                                .product(product)
                                .productVariant(variant)
                                .build();

                WishlistItem saved = wishlistRepository.save(item);
                log.info("Added to wishlist - user: {}, product: {}", user.getId(), product.getId());
                return wishlistMapper.toResponse(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public List<WishlistItemResponse> getCurrentUserWishlist() {
                User user = getCurrentUser();
                return wishlistRepository.findByUserIdWithDetails(user.getId()).stream()
                                .map(wishlistMapper::toResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public void removeFromWishlist(Long itemId) {
                log.info("Removing wishlist item: {}", itemId);
                WishlistItem item = getWishlistItemIfAuthorized(itemId);
                wishlistRepository.delete(item);
        }

        @Override
        public void moveToCart(Long itemId) {
                WishlistItem wishlistItem = getWishlistItemIfAuthorized(itemId);
                User user = wishlistItem.getUser();

                // Get or create cart
                Cart cart = cartRepository.findByUserIdWithItems(user.getId())
                                .orElseGet(() -> {
                                        Cart newCart = Cart.builder()
                                                        .user(user)
                                                        .build();
                                        return cartRepository.save(newCart);
                                });

                // Check if item already in cart
                boolean alreadyInCart = cart.getItems().stream()
                                .anyMatch(ci -> ci.getProduct().getId().equals(wishlistItem.getProduct().getId()) &&
                                                (wishlistItem.getProductVariant() == null ||
                                                                (ci.getProductVariant() != null &&
                                                                                ci.getProductVariant().getId()
                                                                                                .equals(wishlistItem
                                                                                                                .getProductVariant()
                                                                                                                .getId()))));

                if (!alreadyInCart) {
                        CartItem cartItem = CartItem.builder()
                                        .cart(cart)
                                        .product(wishlistItem.getProduct())
                                        .productVariant(wishlistItem.getProductVariant())
                                        .quantity(1)
                                        .build();
                        cart.addItem(cartItem);
                        cartRepository.save(cart);
                        log.info("Moved wishlist item {} to cart for user {}", itemId, user.getId());
                } else {
                        log.info("Item from wishlist {} already in cart, just removing from wishlist", itemId);
                }

                // Remove from wishlist
                wishlistRepository.delete(wishlistItem);
        }

        private User getCurrentUser() {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        }

        private WishlistItem getWishlistItemIfAuthorized(Long itemId) {
                WishlistItem item = wishlistRepository.findById(itemId)
                                .orElseThrow(() -> new ResourceNotFoundException("WishlistItem", "id", itemId));

                User currentUser = getCurrentUser();
                if (!item.getUser().getId().equals(currentUser.getId())) {
                        throw new UnauthorizedException("You are not authorized to access this wishlist item");
                }

                return item;
        }
}
