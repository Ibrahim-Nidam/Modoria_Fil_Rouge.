package com.modoria.domain.product.service.impl;

import com.modoria.domain.category.entity.Category;
import com.modoria.domain.product.entity.Product;
import com.modoria.domain.product.entity.ProductImage;
import com.modoria.domain.season.entity.Season;
import com.modoria.domain.product.enums.ProductStatus;
import com.modoria.domain.product.dto.request.CreateProductRequest;
import com.modoria.domain.product.dto.request.ProductVariantRequest;
import com.modoria.domain.product.dto.request.UpdateProductRequest;
import com.modoria.domain.product.dto.response.ProductResponse;
import com.modoria.domain.product.entity.ProductVariant;
import com.modoria.infrastructure.exceptions.resource.ResourceAlreadyExistsException;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.domain.product.mapper.ProductMapper;
import com.modoria.domain.category.repository.CategoryRepository;
import com.modoria.domain.product.repository.ProductImageRepository;
import com.modoria.domain.product.repository.ProductRepository;
import com.modoria.domain.season.repository.SeasonRepository;
import com.modoria.domain.product.service.ProductService;
import com.modoria.infrastructure.storage.StorageService;
import com.modoria.infrastructure.utils.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of ProductService for product operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final SeasonRepository seasonRepository;
    private final ProductMapper productMapper;
    private final StorageService storageService;

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(CreateProductRequest request) {
        log.info("Creating product: {}", request.getName());

        if (productRepository.existsBySku(request.getSku())) {
            throw ResourceAlreadyExistsException.product(request.getSku());
        }

        Product product = productMapper.toEntity(request);
        product.calculateCompareAtPriceFromDiscount();
        product.setSlug(SlugUtil.generateUniqueSlug(request.getName(),
                slug -> productRepository.existsBySlug(slug)));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> ResourceNotFoundException.category(request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getSeasonIds() != null && !request.getSeasonIds().isEmpty()) {
            Set<Season> seasons = new HashSet<>();
            for (Long seasonId : request.getSeasonIds()) {
                Season season = seasonRepository.findById(seasonId)
                        .orElseThrow(() -> ResourceNotFoundException.season(seasonId));
                seasons.add(season);
            }
            product.setSeasons(seasons);
        }

        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        if (product.getLowStockThreshold() == null) {
            product.setLowStockThreshold(10);
        }

        // Validate quantity
        if ((request.getVariants() == null || request.getVariants().isEmpty()) && request.getQuantity() == null) {
            throw new IllegalArgumentException("Quantity is required if no variants are provided");
        }
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        } else {
            product.setQuantity(0); // Default if using variants, will be updated below
        }

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (ProductVariantRequest vr : request.getVariants()) {
                ProductVariant variant = productMapper.toVariantEntity(vr);
                product.addVariant(variant);
            }
            // Update total quantity based on variants
            int totalStock = product.getVariants().stream()
                    .mapToInt(ProductVariant::getInventoryQuantity)
                    .sum();
            product.setQuantity(totalStock);
        }

        product = productRepository.save(product);
        log.info("Product created with ID: {}", product.getId());

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.product(id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'slug:' + #slug")
    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> ResourceNotFoundException.product(slug));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndStatus(categoryId, ProductStatus.ACTIVE, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getBySeason(Long seasonId, Pageable pageable) {
        return productRepository.findBySeasonId(seasonId, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String query, Pageable pageable) {
        return productRepository.searchProducts(query, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "featured-products")
    public Page<ProductResponse> getFeatured(Pageable pageable) {
        return productRepository.findByIsFeaturedTrueAndStatus(ProductStatus.ACTIVE, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getOnSale(Pageable pageable) {
        return productRepository.findByIsOnSaleTrueAndStatus(ProductStatus.ACTIVE, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getNewArrivals(Pageable pageable) {
        return productRepository.findByIsNewTrueAndStatus(ProductStatus.ACTIVE, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse update(Long id, UpdateProductRequest request) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.product(id));

        // Use Optional to handle nullable fields cleanly
        Optional.ofNullable(request.getName()).ifPresent(name -> {
            product.setName(name);
            product.setSlug(SlugUtil.generateUniqueSlug(name,
                    slug -> productRepository.existsBySlug(slug) && !product.getSlug().equals(slug)));
        });
        Optional.ofNullable(request.getDescription()).ifPresent(product::setDescription);
        Optional.ofNullable(request.getShortDescription()).ifPresent(product::setShortDescription);
        Optional.ofNullable(request.getPrice()).ifPresent(product::setPrice);
        Optional.ofNullable(request.getCompareAtPrice()).ifPresent(product::setCompareAtPrice);
        Optional.ofNullable(request.getPurchasePrice()).ifPresent(product::setPurchasePrice);
        Optional.ofNullable(request.getDiscountPercentage()).ifPresent(discount -> {
            product.setDiscountPercentage(discount);
            product.calculateCompareAtPriceFromDiscount();
        });
        Optional.ofNullable(request.getQuantity()).ifPresent(product::setQuantity);
        Optional.ofNullable(request.getLowStockThreshold()).ifPresent(product::setLowStockThreshold);
        Optional.ofNullable(request.getStatus()).ifPresent(product::setStatus);
        Optional.ofNullable(request.getWeight()).ifPresent(product::setWeight);
        Optional.ofNullable(request.getWeightUnit()).ifPresent(product::setWeightUnit);
        Optional.ofNullable(request.getIsFeatured()).ifPresent(product::setIsFeatured);
        Optional.ofNullable(request.getIsNew()).ifPresent(product::setIsNew);
        Optional.ofNullable(request.getIsOnSale()).ifPresent(product::setIsOnSale);

        // Update category
        Optional.ofNullable(request.getCategoryId()).ifPresent(categoryId -> {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> ResourceNotFoundException.category(categoryId));
            product.setCategory(category);
        });

        // Update seasons
        Optional.ofNullable(request.getSeasonIds()).ifPresent(seasonIds -> {
            Set<Season> seasons = new HashSet<>();
            for (Long seasonId : seasonIds) {
                Season season = seasonRepository.findById(seasonId)
                        .orElseThrow(() -> ResourceNotFoundException.season(seasonId));
                seasons.add(season);
            }
            product.setSeasons(seasons);
        });

        // Update variants
        if (request.getVariants() != null) {
            product.getVariants().clear();
            for (ProductVariantRequest vr : request.getVariants()) {
                ProductVariant variant = productMapper.toVariantEntity(vr);
                product.addVariant(variant);
            }
            int totalStock = product.getVariants().stream()
                    .mapToInt(ProductVariant::getInventoryQuantity)
                    .sum();
            product.setQuantity(totalStock);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product updated: {}", id);

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) {
        log.info("Deleting product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.product(id));

        for (ProductImage image : product.getImages()) {
            storageService.delete(image.getFileName());
        }

        productRepository.delete(product);
        log.info("Product deleted: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponse addImage(Long productId, MultipartFile file, String altText, boolean isPrimary) {
        log.info("Adding image to product: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        String url = storageService.store(file);
        String filename = url.substring(url.lastIndexOf('/') + 1);

        if (isPrimary) {
            imageRepository.clearPrimaryForProduct(productId);
        }

        ProductImage image = ProductImage.builder()
                .url(url)
                .altText(altText)
                .isPrimary(isPrimary || product.getImages().isEmpty())
                .displayOrder(product.getImages().size())
                .fileName(filename)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        product.addImage(image);
        product = productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void removeImage(Long productId, Long imageId) {
        log.info("Removing image {} from product: {}", imageId, productId);

        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> ResourceNotFoundException.product(imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to this product");
        }

        storageService.delete(image.getFileName());
        imageRepository.delete(image);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void setPrimaryImage(Long productId, Long imageId) {
        log.info("Setting primary image {} for product: {}", imageId, productId);

        imageRepository.clearPrimaryForProduct(productId);

        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> ResourceNotFoundException.product(imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to this product");
        }

        image.setIsPrimary(true);
        imageRepository.save(image);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void updateStock(Long productId, int quantity) {
        log.info("Updating stock for product {}: {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        product.setQuantity(quantity);

        if (quantity == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        return productMapper.toResponseList(productRepository.findLowStockProducts());
    }
}
