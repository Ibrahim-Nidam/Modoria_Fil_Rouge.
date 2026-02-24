package com.modoria.domain.order.repository;

import com.modoria.domain.order.entity.Order;
import com.modoria.domain.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

        Optional<Order> findByOrderNumber(String orderNumber);

        Page<Order> findByUserId(Long userId, Pageable pageable);

        Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

        Page<Order> findByStatus(OrderStatus status, Pageable pageable);

        @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
        Optional<Order> findByIdWithItems(@Param("id") Long id);

        @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber")
        Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

        List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

        long countByStatus(OrderStatus status);

        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != 'CANCELLED'")
        BigDecimal calculateTotalRevenue();

        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status != 'CANCELLED'")
        BigDecimal calculateRevenueBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT i.product.id, SUM(i.quantity) FROM OrderItem i WHERE i.order.createdAt BETWEEN :startDate AND :endDate AND i.order.status != 'CANCELLED' GROUP BY i.product.id")
        List<Object[]> findProductSalesStats(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        List<Order> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
