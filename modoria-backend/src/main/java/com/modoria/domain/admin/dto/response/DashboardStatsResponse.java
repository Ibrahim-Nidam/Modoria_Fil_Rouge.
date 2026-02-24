package com.modoria.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long totalOrders;
    private Long totalUsers;
    private Long totalProducts;
    private BigDecimal totalRevenue;
    private Long pendingOrders;
    private Long completedOrders;
    private Long activeProducts;
    private Long outOfStockProducts;
}
