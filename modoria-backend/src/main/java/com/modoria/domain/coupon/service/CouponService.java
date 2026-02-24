package com.modoria.domain.coupon.service;

import com.modoria.domain.coupon.dto.request.CreateCouponRequest;
import com.modoria.domain.coupon.dto.response.CouponResponse;
import com.modoria.domain.coupon.dto.response.CouponValidationResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    CouponResponse create(CreateCouponRequest request);

    CouponResponse getById(Long id);

    List<CouponResponse> getAll();

    CouponValidationResponse validateCoupon(String code, BigDecimal orderTotal);

    void deactivate(Long id);
}
