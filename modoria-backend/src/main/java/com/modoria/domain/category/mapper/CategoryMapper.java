package com.modoria.domain.category.mapper;

import com.modoria.domain.category.entity.Category;
import com.modoria.domain.category.dto.request.CreateCategoryRequest;
import com.modoria.domain.category.dto.response.CategoryResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Simple mapper for Category entity.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "productCount", ignore = true)
    @Mapping(target = "children", ignore = true)
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    @AfterMapping
    default void calculateCategoryFields(@MappingTarget CategoryResponse response, Category category) {
        if (category.getProducts() != null) {
            response.setProductCount(category.getProducts().size());
        }

        // Handle children mapping manually to avoid stack overflow or infinite
        // recursion
        // Simplest way for tree structures in "simple" mappers:
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            // Note: calling toResponseList inside here might cause infinite recursion
            // if we blindly map all children.
            // Better to only map 1 level deep or leave empty for basic list views.
            // For now, let's leave children empty to avoid complexity/StackOverflow as user
            // requested simple mappers.
            // Or better: map them but ensure recursive depth is handled by Logic (not
            // here).
            // Let's just Map them simply:
            // response.setChildren(toResponseList(category.getChildren()));
            // This is DANGEROUS for bidirectional trees. skipping for safety.
        }
    }
}
