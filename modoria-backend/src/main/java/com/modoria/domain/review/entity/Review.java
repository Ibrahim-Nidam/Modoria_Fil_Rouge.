package com.modoria.domain.review.entity;

import com.modoria.domain.base.BaseEntity;
import com.modoria.domain.product.entity.Product;
import com.modoria.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Review entity representing product reviews by customers.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "product_id" })
}, indexes = {
        @Index(name = "idx_review_product", columnList = "product_id"),
        @Index(name = "idx_review_rating", columnList = "rating")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_verified_purchase")
    @Builder.Default
    private Boolean isVerifiedPurchase = false;

    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = true;

    @Column(name = "helpful_count")
    @Builder.Default
    private Integer helpfulCount = 0;

    @Column(name = "not_helpful_count")
    @Builder.Default
    private Integer notHelpfulCount = 0;

    // Admin response
    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    // Validation
    @PrePersist
    @PreUpdate
    public void validateRating() {
        if (rating < 1)
            rating = 1;
        if (rating > 5)
            rating = 5;
    }

    // Helper methods
    public void markHelpful() {
        this.helpfulCount++;
    }

    public void markNotHelpful() {
        this.notHelpfulCount++;
    }
}
