package com.modoria.domain.product.entity;

import com.modoria.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_variant_sku", columnList = "sku"),
        @Index(name = "idx_variant_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "size", length = 10)
    private String size;

    @Column(name = "inventory_quantity", nullable = false)
    @Builder.Default
    private Integer inventoryQuantity = 0;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price; // Override product price if set

    public boolean isInStock() {
        return inventoryQuantity > 0;
    }
}
