package com.modoria.domain.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cart response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
        private Long id;
        private Long userId;
        private List<CartItemResponse> items;
        private int totalItems;
        private int uniqueItemCount;
        private BigDecimal totalAmount;
        private String currency;
}

