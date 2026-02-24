package com.modoria.domain.ai.service;

import com.modoria.domain.order.entity.Order;
import java.util.List;

/**
 * Service to provide order-related knowledge to the AI system.
 */
public interface OrderKnowledgeService {

    /**
     * Get recent orders for a user formatted for AI context.
     * 
     * @param userId The ID of the user
     * @return A formatted string of order history
     */
    String getOrderHistoryContext(Long userId);

    /**
     * Get specific order details formatted for AI context.
     * 
     * @param orderNumber The order number
     * @param userId      The ID of the user (for security)
     * @return A formatted string of order details
     */
    String getOrderDetailsContext(String orderNumber, Long userId);
}
