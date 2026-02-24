package com.modoria.domain.order.dto.request;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create order request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
        private Long addressId;

        @Valid
        private ShippingAddressRequest shippingAddress;

        private String customerNotes;

        private String paymentMethodId;

        private String couponCode;
}
