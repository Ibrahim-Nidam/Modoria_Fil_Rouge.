package com.modoria.domain.ai.service.impl;

import com.modoria.domain.ai.service.AdminInsightService;
import com.modoria.domain.ai.service.OllamaService;
import com.modoria.domain.order.repository.OrderRepository;
import com.modoria.domain.product.repository.ProductRepository;
import com.modoria.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInsightServiceImpl implements AdminInsightService {

        private final OrderRepository orderRepository;
        private final ProductRepository productRepository;
        private final OllamaService ollamaService;
        private final com.modoria.domain.notification.service.NotificationService notificationService;

        @Override
        public String getBusinessSummary() {
                log.info("Generating AI business summary for admin");

                // Gather raw data
                BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
                long openOrders = orderRepository.countByStatus(com.modoria.domain.order.enums.OrderStatus.PENDING);
                long activeProducts = productRepository
                                .countByStatus(com.modoria.domain.product.enums.ProductStatus.ACTIVE);

                // Get last 7 days revenue
                LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
                BigDecimal weeklyRevenue = orderRepository.calculateRevenueBetween(weekAgo, LocalDateTime.now());

                String prompt = String.format(
                                "Summarize this e-commerce business data for an administrator in a professional, concise tone (3-4 sentences):\n"
                                                +
                                                "- Total Lifetime Revenue: $%s\n" +
                                                "- Revenue (Last 7 Days): $%s\n" +
                                                "- Pending Orders: %d\n" +
                                                "- Active Products in Catalog: %d\n" +
                                                "- Highlight any positive trends or areas needing attention.",
                                totalRevenue != null ? totalRevenue.toString() : "0.00",
                                weeklyRevenue != null ? weeklyRevenue.toString() : "0.00",
                                openOrders,
                                activeProducts);

                String response = ollamaService.generateResponse(prompt,
                                "You are a business analytics assistant for Modoria Store.");

                // Notify Admins
                notificationService.notifyRole("ROLE_ADMIN",
                                "Business Summary Ready",
                                "A new AI business summary has been generated.",
                                com.modoria.domain.notification.enums.NotificationType.AI_INSIGHT_READY,
                                "/admin/dashboard",
                                null);

                return response;
        }

        @Override
        public String getInventoryInsights() {
                log.info("Generating AI structured inventory predictions for admin");

                // 1. Gather Context: All products and their 30-day sales
                List<Product> allProducts = productRepository.findAll();
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                List<Object[]> salesStats = orderRepository.findProductSalesStats(thirtyDaysAgo, LocalDateTime.now());

                // Map product ID to sales quantity
                java.util.Map<Long, Long> salesMap = salesStats.stream()
                                .collect(Collectors.toMap(
                                                stat -> (Long) stat[0],
                                                stat -> (Long) stat[1],
                                                (v1, v2) -> v1 // Should not happen with group by
                                ));

                // 2. Format Context for AI
                StringBuilder contextBuilder = new StringBuilder("Modoria Product & 30-Day Sales Data:\n");
                for (Product p : allProducts) {
                        long sold = salesMap.getOrDefault(p.getId(), 0L);
                        contextBuilder.append(String.format(
                                        "- ID: %d | Name: %s | Stock: %d | Sold (30d): %d | Price: $%s\n",
                                        p.getId(), p.getName(), p.getQuantity(), sold, p.getPrice().toString()));
                }

                // 3. Prompt for JSON
                String prompt = """
                                Analyze the sales and inventory data provided.
                                Return a JSON list of products that need reordering.
                                Focus on products with low stock relative to their sales speed.

                                RESPONSE FORMAT (STRICT JSON ONLY):
                                [
                                  {
                                    "productName": "Product Name",
                                    "currentStock": 5,
                                    "reorderAmount": 20,
                                    "newTotal": 25,
                                    "confidence": "95%"
                                  }
                                ]

                                If no products need reordering, return an empty JSON list [].
                                Do not include any text outside the JSON.
                                """;

                return ollamaService.generateResponse(prompt, contextBuilder.toString());
        }
}
