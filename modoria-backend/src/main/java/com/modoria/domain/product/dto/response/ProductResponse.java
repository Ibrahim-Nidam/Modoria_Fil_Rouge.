package com.modoria.domain.product.dto.response;

import com.modoria.domain.product.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Product response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
        private Long id;
        private String name;
        private String slug;
        private String sku;
        private String description;
        private String shortDescription;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private Integer quantity;
        private ProductStatus status;
        private Long categoryId;
        private String categoryName;
        private List<ProductImageResponse> images;
        private String primaryImageUrl;
        private Set<String> seasonNames;
        private Double weight;
        private String weightUnit;
        private boolean isFeatured;
        private boolean isNew;
        private boolean isOnSale;
        private BigDecimal averageRating;
        private Integer reviewCount;
        private BigDecimal discountPercentage;
        private BigDecimal purchasePrice;
        private boolean inStock;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<ProductVariantResponse> variants;
}
