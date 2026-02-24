package com.modoria.domain.product.controller;

import com.modoria.domain.product.dto.request.CreateProductRequest;
import com.modoria.domain.product.dto.request.UpdateProductRequest;
import com.modoria.infrastructure.common.dto.ApiResponse;
import com.modoria.domain.product.dto.response.ProductResponse;
import com.modoria.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for product endpoints.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product API")
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request) {
        log.info("Request to create product: {}", request.getName());
        ProductResponse response = productService.create(request);
        log.info("Product created with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        ProductResponse response = productService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug")
    public ResponseEntity<ApiResponse<ProductResponse>> getBySlug(@PathVariable String slug) {
        ProductResponse response = productService.getBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponse> response = productService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductResponse> response = productService.getByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/season/{seasonId}")
    @Operation(summary = "Get products by season")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getBySeason(
            @PathVariable Long seasonId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductResponse> response = productService.getBySeason(seasonId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductResponse> response = productService.search(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getFeatured(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductResponse> response = productService.getFeatured(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/on-sale")
    @Operation(summary = "Get products on sale")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getOnSale(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductResponse> response = productService.getOnSale(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/new-arrivals")
    @Operation(summary = "Get new arrivals")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getNewArrivals(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductResponse> response = productService.getNewArrivals(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductResponse> response = productService.getByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a product")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("Request to update product: {}", id);
        ProductResponse response = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a product")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Request to delete product: {}", id);
        productService.delete(id);
        log.info("Product {} deleted", id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add image to product")
    public ResponseEntity<ApiResponse<ProductResponse>> addImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary) {
        ProductResponse response = productService.addImage(id, file, altText, isPrimary);
        return ResponseEntity.ok(ApiResponse.success(response, "Image added successfully"));
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove image from product")
    public ResponseEntity<ApiResponse<Void>> removeImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productService.removeImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Image removed successfully"));
    }

    @PatchMapping("/{productId}/images/{imageId}/primary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set primary image")
    public ResponseEntity<ApiResponse<Void>> setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productService.setPrimaryImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Primary image set successfully"));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product stock")
    public ResponseEntity<ApiResponse<Void>> updateStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        productService.updateStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock updated successfully"));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get low stock products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts() {
        List<ProductResponse> response = productService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
