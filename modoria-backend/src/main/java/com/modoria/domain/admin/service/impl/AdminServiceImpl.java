package com.modoria.domain.admin.service.impl;

import com.modoria.domain.admin.dto.response.DashboardStatsResponse;
import com.modoria.domain.admin.service.AdminService;
import com.modoria.domain.order.enums.OrderStatus;
import com.modoria.domain.order.repository.OrderRepository;
import com.modoria.domain.product.enums.ProductStatus;
import com.modoria.domain.product.repository.ProductRepository;
import com.modoria.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        log.info("Fetching dashboard stats");
        Long totalOrders = orderRepository.count();
        Long totalUsers = userRepository.count();
        Long totalProducts = productRepository.count();

        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.PAID)
                .map(order -> order.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        Long completedOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        Long activeProducts = productRepository.countByStatus(ProductStatus.ACTIVE);
        Long outOfStockProducts = productRepository.countByStatus(ProductStatus.OUT_OF_STOCK);

        log.debug("Dashboard stats retrieved - Orders: {}, Revenue: {}", totalOrders, totalRevenue);

        return DashboardStatsResponse.builder()
                .totalOrders(totalOrders)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .activeProducts(activeProducts)
                .outOfStockProducts(outOfStockProducts)
                .build();
    }
}
