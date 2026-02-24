package com.modoria.domain.ai.service.impl;

import com.modoria.domain.ai.service.ProductKnowledgeService;
import com.modoria.domain.product.entity.Product;
import com.modoria.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple keyword-based product knowledge service.
 * Phase 1 implementation - can be upgraded to vector embeddings later.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductKnowledgeServiceImpl implements ProductKnowledgeService {

    private final ProductRepository productRepository;
    private final org.springframework.ai.vectorstore.VectorStore vectorStore;

    // Common stop words to filter out
    private static final List<String> STOP_WORDS = Arrays.asList(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "can", "i", "you", "me", "my", "your");

    @Override
    public List<Product> findRelevantProducts(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        log.info("Performing semantic search for query: {}", query);

        // Perform semantic search via VectorStore
        List<org.springframework.ai.document.Document> documents = vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.query(query)
                        .withTopK(5)
                        .withSimilarityThreshold(0.7));

        if (documents.isEmpty()) {
            log.info("No semantic matches found. Falling back to keyword search.");
            return fallbackKeywordSearch(query);
        }

        // Map documents back to Products
        List<Long> productIds = documents.stream()
                .map(doc -> Long.valueOf(doc.getId()))
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllById(productIds);
        log.info("Found {} semantic product matches", products.size());
        return products;
    }

    private List<Product> fallbackKeywordSearch(String query) {
        List<String> keywords = extractKeywords(query);
        if (keywords.isEmpty())
            return List.of();

        return productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        keywords.get(0),
                        keywords.get(0),
                        PageRequest.of(0, 5))
                .getContent();
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void refreshIndex() {
        log.info("Starting product re-indexing...");
        List<Product> products = productRepository.findAll();

        List<org.springframework.ai.document.Document> documents = products.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());

        vectorStore.accept(documents);
        log.info("Successfully indexed {} products", documents.size());
    }

    private org.springframework.ai.document.Document toDocument(Product p) {
        String content = String.format("Product: %s. Description: %s. Category: %s. SKU: %s.",
                p.getName(),
                p.getDescription() != null ? p.getDescription() : "",
                p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                p.getSku());

        // Store ID in both id and metadata for easy retrieval
        return new org.springframework.ai.document.Document(
                p.getId().toString(),
                content,
                java.util.Map.of("productId", p.getId(), "sku", p.getSku()));
    }

    @Override
    public String buildProductContext(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return "No specific products found for this query. Suggest the customer browse categories or speak to an agent.";
        }

        return products.stream()
                .map(this::formatProductForAI)
                .collect(Collectors.joining("\n\n"));
    }

    @Override
    public String formatProductForAI(Product product) {
        StringBuilder sb = new StringBuilder();
        sb.append("- **").append(product.getName()).append("**");
        sb.append(" (ID: ").append(product.getId()).append(")\n");
        sb.append("  Price: $").append(product.getPrice()).append("\n");

        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            // Truncate long descriptions
            String desc = product.getDescription();
            if (desc.length() > 150) {
                desc = desc.substring(0, 150) + "...";
            }
            sb.append("  Description: ").append(desc).append("\n");
        }

        if (product.getQuantity() != null && product.getQuantity() > 0) {
            sb.append("  In Stock: ").append(product.getQuantity()).append(" units\n");
        } else {
            sb.append("  Currently Out of Stock\n");
        }

        // Add category if available
        if (product.getCategory() != null) {
            sb.append("  Category: ").append(product.getCategory().getName()).append("\n");
        }

        // Add variant info if available
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            sb.append("  Available in:\n");
            product.getVariants().stream()
                    .limit(10) // Limit to avoid token explosion
                    .forEach(v -> {
                        sb.append("    - Size ").append(v.getSize())
                                .append(", Color ").append(v.getColor());
                        if (v.getInventoryQuantity() != null && v.getInventoryQuantity() > 0) {
                            sb.append(" (").append(v.getInventoryQuantity()).append(" in stock)");
                        }
                        sb.append("\n");
                    });
        }

        return sb.toString();
    }

    @Override
    public List<String> extractKeywords(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        // Simple tokenization and filtering
        return Arrays.stream(query.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // Remove special chars
                .split("\\s+"))
                .filter(word -> word.length() > 2) // Min 3 chars
                .filter(word -> !STOP_WORDS.contains(word))
                .distinct()
                .collect(Collectors.toList());
    }
}
