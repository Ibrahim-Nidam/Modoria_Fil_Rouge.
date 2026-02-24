package com.modoria.domain.wishlist.dto.response;

import com.modoria.domain.product.dto.response.ProductResponse;
import com.modoria.domain.product.dto.response.ProductVariantResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {
    private Long id;
    private ProductResponse product;
    private ProductVariantResponse productVariant;
    private LocalDateTime createdAt;
}
