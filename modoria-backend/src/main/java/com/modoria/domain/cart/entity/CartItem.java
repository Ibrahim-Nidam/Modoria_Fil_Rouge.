package com.modoria.domain.cart.entity;

import com.modoria.domain.base.BaseEntity;
import com.modoria.domain.product.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * CartItem entity representing an item in a shopping cart.
 */
@Entity
@Table(name = "cart_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "cart_id", "product_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private com.modoria.domain.product.entity.ProductVariant productVariant;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "price_at_addition", precision = 10, scale = 2)
    private BigDecimal priceAtAddition;

    // Helper methods
    public BigDecimal getSubtotal() {
        BigDecimal currentPrice = priceAtAddition;
        if (productVariant != null && productVariant.getPrice() != null) {
            currentPrice = productVariant.getPrice();
        } else if (product != null) {
            currentPrice = product.getPrice();
        }
        return currentPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }

    public void setQuantity(Integer quantity) {
        this.quantity = Math.max(1, quantity);
    }
}
