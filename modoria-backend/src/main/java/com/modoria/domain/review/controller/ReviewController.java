package com.modoria.domain.review.controller;

import com.modoria.domain.review.dto.request.CreateReviewRequest;
import com.modoria.infrastructure.common.dto.ApiResponse;
import com.modoria.domain.review.dto.response.ReviewResponse;
import com.modoria.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for review endpoints.
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product Review API")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new review")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@Valid @RequestBody CreateReviewRequest request) {
        log.info("Request to create review for product: {}", request.getProductId());
        ReviewResponse response = reviewService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Review created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ApiResponse<ReviewResponse>> getById(@PathVariable Long id) {
        ReviewResponse response = reviewService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews by product")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getByProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getByProduct(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getCurrentUserReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getCurrentUserReviews(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending reviews (Admin)")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getPendingApproval(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getPendingApproval(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a review")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Review updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a review (Admin)")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long id) {
        log.info("Admin approving review: {}", id);
        reviewService.approve(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Review approved successfully"));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a review (Admin)")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long id) {
        reviewService.reject(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Review rejected successfully"));
    }

    @PostMapping("/{id}/helpful")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark review as helpful")
    public ResponseEntity<ApiResponse<Void>> markHelpful(@PathVariable Long id) {
        reviewService.markHelpful(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Marked as helpful"));
    }

    @PostMapping("/{id}/not-helpful")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark review as not helpful")
    public ResponseEntity<ApiResponse<Void>> markNotHelpful(@PathVariable Long id) {
        reviewService.markNotHelpful(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Marked as not helpful"));
    }

    @PostMapping("/{id}/support-response")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Add support/moderator response to review")
    public ResponseEntity<ApiResponse<ReviewResponse>> addSupportResponse(
            @PathVariable Long id,
            @Valid @RequestBody SupportResponseRequest request) {
        ReviewResponse response = reviewService.addSupportResponse(id, request.getResponse());
        return ResponseEntity.ok(ApiResponse.success(response, "Support response added successfully"));
    }

    /**
     * Request DTO for support response.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupportResponseRequest {
        @NotBlank(message = "Response is required")
        private String response;
    }
}
