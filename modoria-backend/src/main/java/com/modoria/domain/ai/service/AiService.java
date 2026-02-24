package com.modoria.domain.ai.service;

import com.modoria.domain.ai.dto.request.AiQueryRequest;
import com.modoria.domain.ai.dto.response.AiResponse;
import com.modoria.domain.product.dto.response.ProductResponse;

import java.util.List;

/**
 * Service interface for AI operations using Ollama.
 */
public interface AiService {

    /**
     * Send a query to the AI model.
     */
    AiResponse query(AiQueryRequest request);

    /**
     * Get product recommendations for a user.
     */
    List<ProductResponse> getRecommendations(Long userId, int limit);

    /**
     * Chat with the AI assistant.
     */
    AiResponse chat(String message, String conversationContext);

    /**
     * Generate a product description.
     */
    String generateProductDescription(String productName, String category, String features);

    /**
     * Check if AI service is available.
     */
    boolean isAvailable();

    /**
     * Get the current model name.
     */
    String getCurrentModel();
}
