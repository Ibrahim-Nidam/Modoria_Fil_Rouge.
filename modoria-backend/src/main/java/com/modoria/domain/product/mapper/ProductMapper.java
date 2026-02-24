package com.modoria.domain.product.mapper;

import com.modoria.domain.product.entity.Product;
import com.modoria.domain.product.entity.ProductImage;
import com.modoria.domain.product.dto.request.CreateProductRequest;
import com.modoria.domain.product.dto.request.ProductVariantRequest;
import com.modoria.domain.product.dto.response.ProductImageResponse;
import com.modoria.domain.product.dto.response.ProductResponse;
import com.modoria.domain.product.dto.response.ProductVariantResponse;
import com.modoria.domain.product.entity.Product;
import com.modoria.domain.product.entity.ProductImage;
import com.modoria.domain.product.entity.ProductVariant;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simple mapper for Product entity.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "seasons", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "variants", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "primaryImageUrl", ignore = true)
    @Mapping(target = "seasonNames", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    @Mapping(target = "purchasePrice", source = "purchasePrice")
    @Mapping(target = "inStock", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", source = "variants")
    ProductResponse toResponse(Product product);

    ProductImageResponse toImageResponse(ProductImage image);

    ProductVariantResponse toVariantResponse(ProductVariant variant);

    ProductVariant toVariantEntity(ProductVariantRequest request);

    List<ProductResponse> toResponseList(List<Product> products);

    @AfterMapping
    default void calculateProductFields(@MappingTarget ProductResponse response, Product product) {
        if (product.getPrimaryImage() != null) {
            response.setPrimaryImageUrl(product.getPrimaryImage().getUrl());
        }

        if (product.getSeasons() != null) {
            Set<String> seasonNames = product.getSeasons().stream()
                    .map(season -> season.getName())
                    .collect(Collectors.toSet());
            response.setSeasonNames(seasonNames);
        }

        if (product.getImages() != null) {
            List<ProductImageResponse> imageResponses = product.getImages().stream()
                    .map(this::toImageResponse)
                    .collect(Collectors.toList());
            response.setImages(imageResponses);
        }

        response.setDiscountPercentage(product.getDiscountPercentage());
        response.setInStock(product.isInStock());
    }
}
