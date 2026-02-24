package com.modoria.domain.category.service.impl;

import com.modoria.domain.category.entity.Category;
import com.modoria.domain.category.dto.request.CreateCategoryRequest;
import com.modoria.domain.category.dto.request.UpdateCategoryRequest;
import com.modoria.domain.category.dto.response.CategoryResponse;
import com.modoria.infrastructure.exceptions.resource.ResourceAlreadyExistsException;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.domain.category.mapper.CategoryMapper;
import com.modoria.domain.category.repository.CategoryRepository;
import com.modoria.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse create(CreateCategoryRequest request) {
        log.info("Creating category: {}", request.getName());
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Category creation failed - name already exists: {}", request.getName());
            throw new ResourceAlreadyExistsException("Category", "name", request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(generateSlug(request.getName()))
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(true)
                .build();

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParent(parent);
            log.debug("Category {} assigned to parent: {}", request.getName(), parent.getName());
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created with ID: {}", saved.getId());
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        return categoryMapper.toResponseList(categoryRepository.findByParentIsNull());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildren(Long parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Category", "id", parentId);
        }
        return categoryMapper.toResponseList(categoryRepository.findByParentId(parentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getFeatured() {
        // Assuming "featured" implies something specific, for now just returning
        // top-level
        // In real app, might depend on a boolean flag or specific logic
        return getRootCategories();
    }

    @Override
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        log.info("Updating category: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (StringUtils.hasText(request.getName()) && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                log.warn("Category update failed - name already exists: {}", request.getName());
                throw new ResourceAlreadyExistsException("Category", "name", request.getName());
            }
            category.setName(request.getName());
            category.setSlug(generateSlug(request.getName()));
        }

        if (StringUtils.hasText(request.getDescription())) {
            category.setDescription(request.getDescription());
        }

        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(category.getId())) {
                log.error("Attempted to set category {} as its own parent", id);
                throw new IllegalArgumentException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParent(parent);
        } else {
            // Null explicitly means disconnect parent (make root)
            category.setParent(null);
        }

        log.info("Category {} updated successfully", id);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting category: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        // Constraint check: ensure no products or subcategories exist or handle cascade
        // For simplicity:
        categoryRepository.deleteById(id);
        log.info("Category {} deleted successfully", id);
    }

    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
