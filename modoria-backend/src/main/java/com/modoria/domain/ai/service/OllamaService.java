package com.modoria.domain.ai.service;

import com.modoria.domain.product.entity.Product;

import java.util.List;

/**
 * Service for interacting with Ollama LLM API.
 */
public interface OllamaService {

    /**
     * Generate a response from the LLM based on the prompt.
     *
     * @param prompt The input prompt
     * @return Generated response text
     */
    String generateResponse(String prompt);

    /**
     * Generate a response with custom context.
     *
     * @param prompt  The user question
     * @param context Additional context to include
     * @return Generated response text
     */
    String generateResponse(String prompt, String context);

    /**
     * Generate a product-related answer with RAG (Retrieval Augmented Generation).
     *
     * @param question Question from customer
     * @param products Relevant products to include in context
     * @return AI-generated answer scoped to products
     */
    String generateProductAnswer(String question, List<Product> products);

    /**
     * Detect if the customer message is requesting human escalation.
     *
     * @param message Customer message
     * @return true if escalation keywords detected
     */
    boolean detectEscalationRequest(String message);

    /**
     * Test connectivity to Ollama service.
     *
     * @return true if service is reachable and responsive
     */
    boolean testConnection();
}
