package com.modoria.domain.ai.service;

/**
 * Service to provide AI-powered business insights for administrators.
 */
public interface AdminInsightService {

    /**
     * Generate a natural language summary of business performance.
     * 
     * @return AI generated insight summary
     */
    String getBusinessSummary();

    /**
     * Get insights specifically about inventory and stock levels.
     * 
     * @return AI generated inventory summary
     */
    String getInventoryInsights();
}
