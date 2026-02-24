package com.modoria.domain.review.service;

import com.modoria.domain.review.dto.request.CreateReviewRequest;
import com.modoria.domain.review.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for review operations.
 */
public interface ReviewService {

    ReviewResponse create(CreateReviewRequest request);

    ReviewResponse getById(Long id);

    Page<ReviewResponse> getByProduct(Long productId, Pageable pageable);

    Page<ReviewResponse> getCurrentUserReviews(Pageable pageable);

    Page<ReviewResponse> getPendingApproval(Pageable pageable);

    ReviewResponse update(Long id, CreateReviewRequest request);

    void delete(Long id);

    void approve(Long id);

    void reject(Long id);

    void markHelpful(Long id);

    void markNotHelpful(Long id);

    ReviewResponse addSupportResponse(Long id, String response);
}
