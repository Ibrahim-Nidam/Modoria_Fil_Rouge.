package com.modoria.domain.coupon.dto.request;

import com.modoria.domain.coupon.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50)
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00")
    private BigDecimal minOrderAmount;

    private LocalDateTime expiryDate;

    @Min(value = 1)
    private Integer usageLimit;
}
