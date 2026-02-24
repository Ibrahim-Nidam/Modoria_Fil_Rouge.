package com.modoria.domain.product.dto.request;

import com.modoria.domain.product.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Update product request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
        @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
        private String name;

        private String description;

        @Size(max = 500, message = "Short description must not exceed 500 characters")
        private String shortDescription;

        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        @DecimalMin(value = "0.00", message = "Compare at price must be non-negative")
        private BigDecimal compareAtPrice;

        @DecimalMin(value = "0.00", message = "Purchase price must be non-negative")
        private BigDecimal purchasePrice;

        @DecimalMin(value = "0.00", message = "Discount percentage must be non-negative")
        @DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100")
        private BigDecimal discountPercentage;

        @Min(value = 0, message = "Quantity must be non-negative")
        private Integer quantity;

        @Min(value = 0, message = "Low stock threshold must be non-negative")
        private Integer lowStockThreshold;

        private ProductStatus status;

        private Long categoryId;

        private Set<Long> seasonIds;

        private Double weight;

        private String weightUnit;

        private Boolean isFeatured;

        private Boolean isNew;

        private Boolean isOnSale;

        private List<ProductVariantRequest> variants;
}
