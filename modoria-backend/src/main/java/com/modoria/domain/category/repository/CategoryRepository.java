package com.modoria.domain.category.repository;

import com.modoria.domain.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    Optional<Category> findByName(String name);

    List<Category> findByParentIsNull();

    List<Category> findByParentId(Long parentId);

    List<Category> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    List<Category> findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentId);

    Page<Category> findByIsActiveTrue(Pageable pageable);

    List<Category> findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.displayOrder ASC")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    Optional<Category> findByIdWithChildren(Long id);
}


