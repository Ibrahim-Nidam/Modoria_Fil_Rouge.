package com.modoria.domain.ai.service;

import com.modoria.domain.product.entity.Product;

import java.util.List;

/**
 * Service for managing product knowledge base for AI responses.
 * Implements simple keyword search for Phase 1 (can be upgraded to vector
 * embeddings later).
 */
public interface ProductKnowledgeService {

    /**
     * Find relevant products based on customer query.
     *
     * @param query Customer's question or search term
     * @return List of relevant products (max 5)
     */
    List<Product> findRelevantProducts(String query);

    /**
     * Build formatted context string from products for LLM.
     *
     * @param products List of products to format
     * @return Formatted string suitable for AI context
     */
    String buildProductContext(List<Product> products);

    /**
     * Format a single product for AI consumption.
     *
     * @param product Product to format
     * @return Formatted product description
     */
    String formatProductForAI(Product product);

    /**
     * Re-indexes all active products into the vector store.
     */
    void refreshIndex();

    /**
     * Extract keywords from customer query for product search.
     *
     * @param query Raw customer question
     * @return List of search keywords
     */
    List<String> extractKeywords(String query);
}
