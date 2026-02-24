package com.modoria.domain.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Category response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String imageUrl;
        private Long parentId;
        private String parentName;
        private List<CategoryResponse> children;
        private Integer displayOrder;
        private boolean isActive;
        private boolean isFeatured;
        private int productCount;
}

