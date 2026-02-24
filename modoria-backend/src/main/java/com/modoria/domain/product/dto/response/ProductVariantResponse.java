package com.modoria.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private String sku;
    private String color;
    private String size;
    private Integer inventoryQuantity;
    private BigDecimal price;
    private boolean inStock; // Calculated
}
