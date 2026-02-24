package com.modoria.domain.order.entity;

import com.modoria.domain.base.BaseEntity;
import com.modoria.domain.product.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * OrderItem entity representing an item within an order.
 * Stores the price at purchase time for historical accuracy.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private com.modoria.domain.product.entity.ProductVariant productVariant;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_sku", length = 50)
    private String productSku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;

    // Helper methods
    public void calculateSubtotal() {
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @PrePersist
    @PreUpdate
    public void preSave() {
        if (product != null) {
            this.productName = product.getName();
            this.productSku = product.getSku();

            if (productVariant != null) {
                this.productSku = productVariant.getSku();
                // Append variant details to name if possible, or just rely on SKU.
                // Let's explicitly format it: Name - Color Size
                StringBuilder details = new StringBuilder();
                if (productVariant.getColor() != null)
                    details.append(" ").append(productVariant.getColor());
                if (productVariant.getSize() != null)
                    details.append(" ").append(productVariant.getSize());
                if (details.length() > 0) {
                    this.productName = product.getName() + " -" + details.toString();
                }
            }

            if (product.getPrimaryImage() != null) {
                this.productImageUrl = product.getPrimaryImage().getUrl();
            }
        }
        calculateSubtotal();
    }
}
