package com.modoria.domain.coupon.mapper;

import com.modoria.domain.coupon.dto.request.CreateCouponRequest;
import com.modoria.domain.coupon.dto.response.CouponResponse;
import com.modoria.domain.coupon.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Coupon toEntity(CreateCouponRequest request);

    CouponResponse toResponse(Coupon coupon);
}
