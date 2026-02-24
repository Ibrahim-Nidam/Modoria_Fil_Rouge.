package com.modoria.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for shipping address details in an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressRequest {
        @NotBlank(message = "First name is required")
        @Size(max = 100)
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        private String lastName;

        @NotBlank(message = "Address is required")
        @Size(max = 255)
        private String addressLine1;

        @Size(max = 255)
        private String addressLine2;

        @NotBlank(message = "City is required")
        @Size(max = 100)
        private String city;

        @Size(max = 100)
        private String state;

        @NotBlank(message = "Postal code is required")
        @Size(max = 20)
        private String postalCode;

        @NotBlank(message = "Country is required")
        @Size(max = 100)
        private String country;

        @Size(max = 20)
        private String phone;
}

