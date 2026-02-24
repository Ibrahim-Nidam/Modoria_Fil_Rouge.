package com.modoria.domain.wishlist.repository;

import com.modoria.domain.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    @Query("SELECT w FROM WishlistItem w " +
            "LEFT JOIN FETCH w.product p " +
            "LEFT JOIN FETCH w.productVariant pv " +
            "WHERE w.user.id = :userId")
    List<WishlistItem> findByUserIdWithDetails(Long userId);

    Optional<WishlistItem> findByUserIdAndProductIdAndProductVariantId(
            Long userId, Long productId, Long productVariantId);

    boolean existsByUserIdAndProductIdAndProductVariantId(
            Long userId, Long productId, Long productVariantId);
}
