package com.modoria.domain.product.repository;

import com.modoria.domain.product.entity.Product;
import com.modoria.domain.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

        Optional<Product> findBySlug(String slug);

        Optional<Product> findBySku(String sku);

        boolean existsBySlug(String slug);

        boolean existsBySku(String sku);

        Page<Product> findByStatus(ProductStatus status, Pageable pageable);

        Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

        Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);

        @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
        Page<Product> searchProducts(@Param("search") String search, Pageable pageable);

        Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description,
                        Pageable pageable);

        Page<Product> findByIsFeaturedTrueAndStatus(ProductStatus status, Pageable pageable);

        Page<Product> findByIsOnSaleTrueAndStatus(ProductStatus status, Pageable pageable);

        Page<Product> findByIsNewTrueAndStatus(ProductStatus status, Pageable pageable);

        @Query("SELECT p FROM Product p JOIN p.seasons s WHERE s.id = :seasonId")
        Page<Product> findBySeasonId(@Param("seasonId") Long seasonId, Pageable pageable);

        @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
        Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        Pageable pageable);

        @Query("SELECT p FROM Product p WHERE p.quantity <= p.lowStockThreshold")
        List<Product> findLowStockProducts();

        @Query("SELECT p FROM Product p WHERE p.quantity = 0")
        List<Product> findOutOfStockProducts();

        long countByStatus(ProductStatus status);
}
