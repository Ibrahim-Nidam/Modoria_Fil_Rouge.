package com.modoria.domain.product.entity;

import com.modoria.domain.base.BaseEntity;
import com.modoria.domain.category.entity.Category;
import com.modoria.domain.product.enums.ProductStatus;
import com.modoria.domain.review.entity.Review;
import com.modoria.domain.season.entity.Season;

import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Product entity representing items for sale.
 * Includes pricing, inventory, and seasonal associations.
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_slug", columnList = "slug"),
        @Index(name = "idx_product_sku", columnList = "sku"),
        @Index(name = "idx_product_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "product_seasons", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "season_id"))
    @Builder.Default
    private Set<Season> seasons = new HashSet<>();

    @Column(name = "weight")
    private Double weight;

    @Column(name = "weight_unit", length = 10)
    @Builder.Default
    private String weightUnit = "kg";

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_new")
    @Builder.Default
    private Boolean isNew = false;

    @Column(name = "is_on_sale")
    @Builder.Default
    private Boolean isOnSale = false;

    @Column(name = "average_rating", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    // Helper methods
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    public ProductImage getPrimaryImage() {
        return images.stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }

    public boolean isInStock() {
        return quantity > 0 && status == ProductStatus.ACTIVE;
    }

    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }

    public BigDecimal getDiscountPercentage() {
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            return discountPercentage;
        }
        if (compareAtPrice != null && compareAtPrice.compareTo(price) > 0) {
            return compareAtPrice.subtract(price)
                    .divide(compareAtPrice, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * If discountPercentage is set, calculates the compareAtPrice (original price).
     * newPrice = originalPrice * (1 - discount/100) -> originalPrice = newPrice /
     * (1 - discount/100)
     */
    public void calculateCompareAtPriceFromDiscount() {
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0 && price != null) {
            BigDecimal multiplier = BigDecimal.ONE
                    .subtract(discountPercentage.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP));
            if (multiplier.compareTo(BigDecimal.ZERO) > 0) {
                this.compareAtPrice = price.divide(multiplier, 2, java.math.RoundingMode.HALF_UP);
            }
        }
    }

    public void updateAverageRating() {
        if (reviews.isEmpty()) {
            this.averageRating = BigDecimal.ZERO;
            this.reviewCount = 0;
        } else {
            double avg = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            this.averageRating = BigDecimal.valueOf(avg);
            this.reviewCount = reviews.size();
        }
    }
}
