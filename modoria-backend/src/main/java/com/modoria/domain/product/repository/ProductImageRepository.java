package com.modoria.domain.product.repository;
import com.modoria.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductImage entity operations.
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);

    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isPrimary = false WHERE pi.product.id = :productId")
    void clearPrimaryForProduct(@Param("productId") Long productId);

    void deleteByProductId(Long productId);

    int countByProductId(Long productId);
}


