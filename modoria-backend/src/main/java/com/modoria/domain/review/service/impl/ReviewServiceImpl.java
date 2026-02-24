package com.modoria.domain.review.service.impl;

import com.modoria.domain.product.entity.Product;
import com.modoria.domain.review.entity.Review;
import com.modoria.domain.user.entity.User;
import com.modoria.domain.review.dto.request.CreateReviewRequest;
import com.modoria.domain.review.dto.response.ReviewResponse;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.domain.review.mapper.ReviewMapper;
import com.modoria.domain.product.repository.ProductRepository;
import com.modoria.domain.review.repository.ReviewRepository;
import com.modoria.domain.user.repository.UserRepository;
import com.modoria.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final com.modoria.domain.notification.service.NotificationService notificationService;

    @Override
    public ReviewResponse create(CreateReviewRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Check if user already reviewed this product
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            log.warn("User {} attempted duplicate review for product {}", user.getId(), product.getId());
            throw com.modoria.infrastructure.exceptions.resource.ResourceAlreadyExistsException
                    .review(user.getId(), product.getId());
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isVerifiedPurchase(false) // Logic to check orders would go here
                .isApproved(false) // Reviews pending approval by default
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Review created by user {} for product {}, pending approval", user.getId(), product.getId());

        // Notify Admins/Support
        notificationService.notifyRole("ROLE_ADMIN",
                "New Product Review",
                "A new review has been posted for: " + product.getName(),
                com.modoria.domain.notification.enums.NotificationType.SYSTEM_ALERT,
                "/admin/reviews/" + savedReview.getId(),
                "{\"reviewId\":" + savedReview.getId() + "}");

        notificationService.notifyRole("ROLE_SUPPORT",
                "New Product Review",
                "A new review has been posted for: " + product.getName(),
                com.modoria.domain.notification.enums.NotificationType.SYSTEM_ALERT,
                "/admin/reviews/" + savedReview.getId(),
                "{\"reviewId\":" + savedReview.getId() + "}");

        return reviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getByProduct(Long productId, Pageable pageable) {
        // Typically show only approved reviews publicly
        return reviewRepository.findByProductIdAndIsApprovedTrue(productId, pageable)
                .map(reviewMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getCurrentUserReviews(Pageable pageable) {
        User user = getCurrentUser();
        return reviewRepository.findByUserId(user.getId(), pageable)
                .map(reviewMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPendingApproval(Pageable pageable) {
        return reviewRepository.findByIsApprovedFalse(pageable)
                .map(reviewMapper::toResponse);
    }

    @Override
    public ReviewResponse update(Long id, CreateReviewRequest request) {
        User user = getCurrentUser();
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        // Ensure user owns the review
        if (!review.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted to update review {} owned by another user", user.getId(), id);
            // Throw access denied or not found
            throw new ResourceNotFoundException("Review", "id", id);
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setIsApproved(false); // Re-approval needed after edit

        log.info("Review {} updated by user {}, set to pending approval", id, user.getId());
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting review: {}", id);
        // Similar ownership check needed or Admin check
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review", "id", id);
        }
        reviewRepository.deleteById(id);
        log.info("Review {} deleted", id);
    }

    @Override
    public void approve(Long id) {
        log.info("Approving review: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        review.setIsApproved(true);
        reviewRepository.save(review);
        log.info("Review {} approved", id);
    }

    @Override
    public void reject(Long id) {
        // Hard delete or status update? Assuming simple delete for rejection or a
        // status field if one existed
        // For now, let's just delete rejected reviews or keep them unapproved.
        // Let's assume we keep them unapproved but maybe add a flag later.
        // Or simply delete:
        delete(id);
    }

    @Override
    public void markHelpful(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    @Override
    public void markNotHelpful(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        review.setNotHelpfulCount(review.getNotHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    @Override
    public ReviewResponse addSupportResponse(Long id, String response) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        review.setAdminResponse(response); // Using existing field, will be named supportResponse in DTO
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
