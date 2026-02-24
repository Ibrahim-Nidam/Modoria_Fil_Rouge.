package com.modoria.domain.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {

    @NotNull(message = "SKU is required")
    private String sku;

    private String color;

    private String size;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer inventoryQuantity;

    private BigDecimal price;
}
