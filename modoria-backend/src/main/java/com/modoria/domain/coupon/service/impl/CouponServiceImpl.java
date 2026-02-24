package com.modoria.domain.coupon.service.impl;

import com.modoria.domain.coupon.dto.request.CreateCouponRequest;
import com.modoria.domain.coupon.dto.response.CouponResponse;
import com.modoria.domain.coupon.dto.response.CouponValidationResponse;
import com.modoria.domain.coupon.entity.Coupon;
import com.modoria.domain.coupon.mapper.CouponMapper;
import com.modoria.domain.coupon.repository.CouponRepository;
import com.modoria.domain.coupon.service.CouponService;
import com.modoria.infrastructure.exceptions.resource.ResourceAlreadyExistsException;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Override
    public CouponResponse create(CreateCouponRequest request) {
        log.info("Creating new coupon with code: {}", request.getCode());
        if (couponRepository.existsByCode(request.getCode())) {
            log.warn("Coupon creation failed - code already exists: {}", request.getCode());
            throw new ResourceAlreadyExistsException("Coupon with code " + request.getCode() + " already exists");
        }

        Coupon coupon = couponMapper.toEntity(request);
        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon created successfully: {}", saved.getCode());
        return couponMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        return couponMapper.toResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getAll() {
        return couponRepository.findAll().stream()
                .map(couponMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(String code, BigDecimal orderTotal) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElse(null);

        if (coupon == null) {
            log.debug("Coupon validation failed - not found or inactive: {}", code);
            return CouponValidationResponse.builder()
                    .valid(false)
                    .message("Coupon not found or inactive")
                    .build();
        }

        if (!coupon.isValid()) {
            log.debug("Coupon validation failed - expired or limit reached: {}", code);
            String message = "Coupon has expired or reached usage limit";
            return CouponValidationResponse.builder()
                    .valid(false)
                    .message(message)
                    .build();
        }

        if (coupon.getMinOrderAmount() != null && orderTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            log.debug("Coupon validation failed - min order amount not met: {}", code);
            return CouponValidationResponse.builder()
                    .valid(false)
                    .message("Minimum order amount not met. Required: " + coupon.getMinOrderAmount())
                    .build();
        }

        BigDecimal discount = coupon.calculateDiscount(orderTotal);
        log.info("Coupon validated successfully: {}, discount: {}", code, discount);
        return CouponValidationResponse.builder()
                .valid(true)
                .message("Coupon applied successfully")
                .discountAmount(discount)
                .couponCode(code)
                .build();
    }

    @Override
    public void deactivate(Long id) {
        log.info("Deactivating coupon: {}", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        coupon.setIsActive(false);
        couponRepository.save(coupon);
        log.info("Coupon deactivated: {}", id);
    }
}
