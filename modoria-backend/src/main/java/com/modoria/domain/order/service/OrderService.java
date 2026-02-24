package com.modoria.domain.order.service;

import com.modoria.domain.order.enums.OrderStatus;
import com.modoria.domain.order.dto.request.CreateOrderRequest;
import com.modoria.domain.order.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for order operations.
 */
public interface OrderService {

    OrderResponse create(CreateOrderRequest request);

    OrderResponse getById(Long id);

    OrderResponse getByOrderNumber(String orderNumber);

    Page<OrderResponse> getCurrentUserOrders(Pageable pageable);

    Page<OrderResponse> getUserOrders(Long userId, Pageable pageable);

    Page<OrderResponse> getAll(Pageable pageable);

    Page<OrderResponse> getByStatus(OrderStatus status, Pageable pageable);

    OrderResponse updateStatus(Long id, OrderStatus status);

    OrderResponse cancel(Long id);

    OrderResponse markAsPaid(Long id, String paymentId);

    OrderResponse markAsShipped(Long id, String trackingNumber);

    OrderResponse markAsDelivered(Long id);
}





