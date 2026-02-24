package com.modoria.domain.category.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Update category request DTO.
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update category request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        private String imageUrl;

        private Long parentId;

        private Integer displayOrder;

        private Boolean isActive;

        private Boolean isFeatured;
}

