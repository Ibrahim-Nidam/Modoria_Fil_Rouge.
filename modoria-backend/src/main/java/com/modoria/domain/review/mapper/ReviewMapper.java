package com.modoria.domain.review.mapper;

import com.modoria.domain.review.entity.Review;
import com.modoria.domain.review.dto.response.ReviewResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Simple mapper for Review entity.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ReviewMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userAvatar", source = "user.avatarUrl")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "supportResponse", source = "adminResponse")
    ReviewResponse toResponse(Review review);

    List<ReviewResponse> toResponseList(List<Review> reviews);

    @AfterMapping
    default void populateUserName(@MappingTarget ReviewResponse response, Review review) {
        if (review.getUser() != null) {
            response.setUserName(review.getUser().getFullName());
        }
    }
}
