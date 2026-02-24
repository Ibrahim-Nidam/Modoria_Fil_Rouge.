package com.modoria.domain.ai.service.impl;

import com.modoria.domain.ai.dto.OllamaRequest;
import com.modoria.domain.ai.dto.OllamaResponse;
import com.modoria.domain.ai.service.OllamaService;
import com.modoria.domain.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OllamaServiceImpl implements OllamaService {

    private final org.springframework.web.client.RestClient restClient;
    private final String model;
    private final Double temperature;
    private final Integer maxTokens;

    // Escalation keywords pattern
    private static final Pattern ESCALATION_PATTERN = Pattern.compile(
            "(?i)(speak to|talk to|human|agent|person|representative|someone|help me|customer service)",
            Pattern.CASE_INSENSITIVE);

    public OllamaServiceImpl(
            @Value("${modoria.ai.ollama.url}") String ollamaUrl,
            @Value("${modoria.ai.ollama.model}") String model,
            @Value("${modoria.ai.ollama.temperature:0.7}") Double temperature,
            @Value("${modoria.ai.ollama.max-tokens:500}") Integer maxTokens) {
        this.restClient = org.springframework.web.client.RestClient.builder()
                .baseUrl(ollamaUrl)
                .build();
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;

        log.info("OllamaService initialized with model: {}, url: {}", model, ollamaUrl);
    }

    @Override
    public String generateResponse(String prompt) {
        return generateResponse(prompt, null);
    }

    @Override
    public String generateResponse(String prompt, String context) {
        String fullPrompt = context != null ? context + "\n\n" + prompt : prompt;

        OllamaRequest request = OllamaRequest.builder()
                .model(model)
                .prompt(fullPrompt)
                .stream(false)
                .options(OllamaRequest.Options.builder()
                        .temperature(temperature)
                        .numPredict(maxTokens)
                        .build())
                .build();

        try {
            log.debug("Sending request to Ollama: {}", request);

            OllamaResponse response = restClient.post()
                    .uri("/api/generate")
                    .body(request)
                    .retrieve()
                    .body(OllamaResponse.class);

            if (response != null && response.getResponse() != null) {
                log.debug("Received response from Ollama: {} chars", response.getResponse().length());
                return response.getResponse().trim();
            }

            log.warn("Ollama returned null or empty response");
            return "I'm having trouble processing your request right now. Would you like to speak to a human agent?";

        } catch (Exception e) {
            log.error("Error calling Ollama API", e);
            return "I'm currently unavailable. Let me connect you with a human agent.";
        }
    }

    @Override
    public String generateProductAnswer(String question, List<Product> products) {
        String systemPrompt = buildSystemPrompt();
        String productContext = buildProductContext(products);
        String fullPrompt = systemPrompt + "\n\n" + productContext + "\n\nCustomer Question: " + question;

        return generateResponse(fullPrompt);
    }

    @Override
    public boolean detectEscalationRequest(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        return ESCALATION_PATTERN.matcher(message).find();
    }

    @Override
    public boolean testConnection() {
        try {
            OllamaRequest request = OllamaRequest.builder()
                    .model(model)
                    .prompt("Hello")
                    .stream(false)
                    .build();

            OllamaResponse response = restClient.post()
                    .uri("/api/generate")
                    .body(request)
                    .retrieve()
                    .body(OllamaResponse.class);

            return response != null && response.getResponse() != null;
        } catch (Exception e) {
            log.error("Ollama connection test failed", e);
            return false;
        }
    }

    /**
     * Build the system prompt that scopes the AI to store-related questions only.
     */
    private String buildSystemPrompt() {
        return """
                You are Modoria's AI shopping assistant. Your role is to help customers with:
                - Product information, sizing, colors, materials, availability
                - General shopping questions about our store
                - Order status queries (refer them to their account page)
                - Return and shipping policies

                IMPORTANT RULES:
                - Only answer questions about Modoria products and policies
                - If asked about topics outside the store (weather, cooking, news, etc.),
                  politely say: "I can only help with questions about our products and store.
                  Would you like to speak with a human agent?"
                - Keep responses concise, friendly, and helpful (2-3 sentences max)
                - If the customer seems frustrated or confused, suggest speaking to a human agent
                - If you don't have specific product information, suggest speaking to an agent

                Available Products Below:
                """;
    }

    /**
     * Format products into context string for the LLM.
     */
    private String buildProductContext(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return "No specific products found for this query. Suggest the customer browse categories or speak to an agent.";
        }

        return products.stream()
                .limit(5) // Limit to 5 products to avoid token limits
                .map(this::formatProductForAI)
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * Format a single product for AI context.
     */
    private String formatProductForAI(Product product) {
        StringBuilder sb = new StringBuilder();
        sb.append("- **").append(product.getName()).append("**");
        sb.append(" (ID: ").append(product.getId()).append(")\n");
        sb.append("  Price: $").append(product.getPrice()).append("\n");
        sb.append("  Description: ").append(product.getDescription()).append("\n");

        if (product.getQuantity() != null && product.getQuantity() > 0) {
            sb.append("  In Stock: ").append(product.getQuantity()).append(" units\n");
        } else {
            sb.append("  Out of Stock\n");
        }

        // Add variant info if available
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            sb.append("  Available Options: ");
            String variantInfo = product.getVariants().stream()
                    .map(v -> v.getSize() + "/" + v.getColor())
                    .distinct()
                    .collect(Collectors.joining(", "));
            sb.append(variantInfo);
        }

        return sb.toString();
    }
}
