package com.modoria.domain.ai.service.impl;

import com.modoria.domain.ai.service.OrderKnowledgeService;
import com.modoria.domain.order.entity.Order;
import com.modoria.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderKnowledgeServiceImpl implements OrderKnowledgeService {

    private final OrderRepository orderRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @Override
    @Transactional(readOnly = true)
    public String getOrderHistoryContext(Long userId) {
        log.debug("Fetching order history context for user: {}", userId);
        List<Order> orders = orderRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);

        if (orders.isEmpty()) {
            return "The customer has no previous orders.";
        }

        StringBuilder sb = new StringBuilder("Customer's Order History:\n");
        for (Order order : orders) {
            sb.append("- Order #").append(order.getOrderNumber())
                    .append(" | Date: ").append(order.getCreatedAt().format(DATE_FORMATTER))
                    .append(" | Status: ").append(order.getStatus())
                    .append(" | Total: $").append(order.getTotalAmount())
                    .append("\n");
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public String getOrderDetailsContext(String orderNumber, Long userId) {
        log.debug("Fetching order details context for order: {} and user: {}", orderNumber, userId);
        return orderRepository.findByOrderNumber(orderNumber)
                .filter(order -> order.getUser().getId().equals(userId))
                .map(this::formatOrderFullDetails)
                .orElse("Order not found or access denied.");
    }

    private String formatOrderFullDetails(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order Details for #").append(order.getOrderNumber()).append(":\n");
        sb.append("- Status: ").append(order.getStatus()).append("\n");
        sb.append("- Date: ").append(order.getCreatedAt().format(DATE_FORMATTER)).append("\n");
        sb.append("- Total: $").append(order.getTotalAmount()).append("\n");
        sb.append("- Items:\n");

        order.getItems().forEach(item -> {
            sb.append("  * ").append(item.getQuantity()).append("x ")
                    .append(item.getProduct().getName())
                    .append(" ($").append(item.getUnitPrice()).append(" each)\n");
        });

        if (order.getTrackingNumber() != null) {
            sb.append("- Tracking Number: ").append(order.getTrackingNumber()).append("\n");
        }

        return sb.toString();
    }
}
