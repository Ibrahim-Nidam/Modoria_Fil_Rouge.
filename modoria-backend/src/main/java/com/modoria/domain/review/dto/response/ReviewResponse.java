package com.modoria.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Review response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
        private Long id;
        private Long userId;
        private String userName;
        private String userAvatar;
        private Long productId;
        private String productName;
        private Integer rating;
        private String title;
        private String comment;
        private boolean isVerifiedPurchase;
        private int helpfulCount;
        private int notHelpfulCount;
        private String supportResponse; // Response from support/moderator
        private LocalDateTime createdAt;
}
