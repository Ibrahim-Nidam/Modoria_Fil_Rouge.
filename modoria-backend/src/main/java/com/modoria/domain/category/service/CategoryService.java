package com.modoria.domain.category.service;

import com.modoria.domain.category.dto.request.CreateCategoryRequest;
import com.modoria.domain.category.dto.request.UpdateCategoryRequest;
import com.modoria.domain.category.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for category operations.
 */
public interface CategoryService {

    CategoryResponse create(CreateCategoryRequest request);

    CategoryResponse getById(Long id);

    CategoryResponse getBySlug(String slug);

    Page<CategoryResponse> getAll(Pageable pageable);

    List<CategoryResponse> getRootCategories();

    List<CategoryResponse> getChildren(Long parentId);

    List<CategoryResponse> getFeatured();

    CategoryResponse update(Long id, UpdateCategoryRequest request);

    void delete(Long id);
}




