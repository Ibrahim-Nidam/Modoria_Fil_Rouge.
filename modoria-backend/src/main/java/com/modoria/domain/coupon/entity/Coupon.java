package com.modoria.domain.coupon.entity;

import com.modoria.domain.base.BaseEntity;
import com.modoria.domain.coupon.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Coupon extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public boolean isValid() {
        if (!isActive)
            return false;
        if (expiryDate != null && LocalDateTime.now().isAfter(expiryDate))
            return false;
        if (usageLimit != null && usageCount >= usageLimit)
            return false;
        return true;
    }

    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        if (discountType == DiscountType.PERCENTAGE) {
            return orderTotal.multiply(discountValue).divide(new BigDecimal("100"));
        } else {
            return discountValue;
        }
    }
}
