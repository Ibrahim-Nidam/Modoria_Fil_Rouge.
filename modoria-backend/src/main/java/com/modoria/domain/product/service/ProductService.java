package com.modoria.domain.product.service;

import com.modoria.domain.product.dto.request.CreateProductRequest;
import com.modoria.domain.product.dto.request.UpdateProductRequest;
import com.modoria.domain.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for product operations.
 */
public interface ProductService {

    ProductResponse create(CreateProductRequest request);

    ProductResponse getById(Long id);

    ProductResponse getBySlug(String slug);

    Page<ProductResponse> getAll(Pageable pageable);

    Page<ProductResponse> getByCategory(Long categoryId, Pageable pageable);

    Page<ProductResponse> getBySeason(Long seasonId, Pageable pageable);

    Page<ProductResponse> search(String query, Pageable pageable);

    Page<ProductResponse> getFeatured(Pageable pageable);

    Page<ProductResponse> getOnSale(Pageable pageable);

    Page<ProductResponse> getNewArrivals(Pageable pageable);

    Page<ProductResponse> getByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    ProductResponse update(Long id, UpdateProductRequest request);

    void delete(Long id);

    ProductResponse addImage(Long productId, MultipartFile file, String altText, boolean isPrimary);

    void removeImage(Long productId, Long imageId);

    void setPrimaryImage(Long productId, Long imageId);

    void updateStock(Long productId, int quantity);

    List<ProductResponse> getLowStockProducts();
}




