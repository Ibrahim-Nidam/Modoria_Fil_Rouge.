package com.modoria.domain.coupon.controller;

import com.modoria.domain.coupon.dto.request.CreateCouponRequest;
import com.modoria.domain.coupon.dto.response.CouponResponse;
import com.modoria.domain.coupon.dto.response.CouponValidationResponse;
import com.modoria.domain.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Coupon management")
@Slf4j
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new coupon (Admin only)")
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CreateCouponRequest request) {
        log.info("Request to create coupon: {}", request.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all coupons (Admin only)")
    public ResponseEntity<List<CouponResponse>> getAll() {
        return ResponseEntity.ok(couponService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get coupon by ID (Admin only)")
    public ResponseEntity<CouponResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getById(id));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate a coupon code")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal orderTotal) {
        return ResponseEntity.ok(couponService.validateCoupon(code, orderTotal));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a coupon (Admin only)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        log.info("Request to deactivate coupon: {}", id);
        couponService.deactivate(id);
    }
}
