package com.modoria.domain.cart.service.impl;

import com.modoria.domain.cart.entity.Cart;
import com.modoria.domain.cart.entity.CartItem;
import com.modoria.domain.product.entity.Product;
import com.modoria.domain.user.entity.User;
import com.modoria.domain.cart.dto.request.AddToCartRequest;
import com.modoria.domain.cart.dto.request.UpdateCartItemRequest;
import com.modoria.domain.cart.dto.response.CartResponse;
import com.modoria.infrastructure.exceptions.business.InsufficientStockException;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.domain.cart.mapper.CartMapper;
import com.modoria.domain.cart.repository.CartRepository;
import com.modoria.domain.product.repository.ProductRepository;
import com.modoria.domain.user.repository.UserRepository;
import com.modoria.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse addItem(AddToCartRequest request) {
        log.info("Adding item to cart - productId: {}, quantity: {}", request.getProductId(), request.getQuantity());
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        com.modoria.domain.product.entity.ProductVariant variant = null;
        if (request.getProductVariantId() != null) {
            variant = product.getVariants().stream()
                    .filter(v -> v.getId().equals(request.getProductVariantId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", request.getProductVariantId()));

            if (variant.getInventoryQuantity() < request.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for variant: " + variant.getSku());
            }
        } else {
            if (product.getQuantity() < request.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName());
            }
        }

        final com.modoria.domain.product.entity.ProductVariant finalVariant = variant;
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()) &&
                        ((item.getProductVariant() == null && finalVariant == null) ||
                                (item.getProductVariant() != null && finalVariant != null
                                        && item.getProductVariant().getId().equals(finalVariant.getId()))))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            // Check stock again
            if (finalVariant != null) {
                if (finalVariant.getInventoryQuantity() < newQuantity) {
                    throw new InsufficientStockException("Not enough stock for variant: " + finalVariant.getSku());
                }
            } else {
                if (product.getQuantity() < newQuantity) {
                    throw new InsufficientStockException("Not enough stock for product: " + product.getName());
                }
            }
            item.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .productVariant(finalVariant)
                    .quantity(request.getQuantity())
                    .priceAtAddition(finalVariant != null && finalVariant.getPrice() != null ? finalVariant.getPrice()
                            : product.getPrice())
                    .build();
            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("Item added to cart for user: {}, total items: {}", user.getEmail(), savedCart.getTotalItems());
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse updateItem(Long productId, UpdateCartItemRequest request) {
        log.info("Updating cart item - productId: {}, new quantity: {}", productId, request.getQuantity());
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        // Find item matching productId AND variantId (if provided)
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .filter(i -> {
                    if (request.getProductVariantId() != null) {
                        return i.getProductVariant() != null
                                && i.getProductVariant().getId().equals(request.getProductVariantId());
                    } else {
                        // If variantId not provided, assume null variant or return first match
                        // (ambiguous but compliant with old API)
                        // Better to strict match null if we want to be safe, but legacy behavior
                        // implies any.
                        // Let's match null variant if request id is null, OR if ambiguity exists return
                        // first.
                        // Ideally checking variantId is required if variants exist.
                        return true;
                    }
                })
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", productId));

        Product product = item.getProduct();
        // Check stock
        if (item.getProductVariant() != null) {
            if (item.getProductVariant().getInventoryQuantity() < request.getQuantity()) {
                throw new InsufficientStockException("stock for variant");
            }
        } else {
            if (product.getQuantity() < request.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName());
            }
        }

        item.setQuantity(request.getQuantity());
        Cart savedCart = cartRepository.save(cart);
        log.debug("Cart item updated for user: {}", user.getEmail());
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse removeItem(Long productId, Long variantId) {
        log.info("Removing item from cart - productId: {}, variantId: {}", productId, variantId);
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        CartItem itemToRemove = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .filter(i -> {
                    if (variantId != null) {
                        return i.getProductVariant() != null && i.getProductVariant().getId().equals(variantId);
                    }
                    // If variantId null, maybe remove item with null variant?
                    // Or match any?
                    // Let's say if variantId is null, we look for item with null variant.
                    return i.getProductVariant() == null;
                })
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", productId));

        cart.removeItem(itemToRemove);
        Cart savedCart = cartRepository.save(cart);
        log.info("Item removed from cart, remaining items: {}", savedCart.getTotalItems());
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse clearCart() {
        log.info("Clearing cart");
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        int previousItems = cart.getTotalItems();
        cart.clearItems();
        Cart savedCart = cartRepository.save(cart);
        log.info("Cart cleared for user: {}, removed {} items", user.getEmail(), previousItems);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount() {
        User user = getCurrentUser();
        return cartRepository.findByUserId(user.getId())
                .map(Cart::getTotalItems)
                .orElse(0);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .currency("MAD")
                            .build();
                    return cartRepository.save(newCart);
                });
    }
}
