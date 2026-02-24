package com.modoria.domain.order.controller;

import com.modoria.domain.order.enums.OrderStatus;
import com.modoria.domain.order.dto.request.CreateOrderRequest;
import com.modoria.infrastructure.common.dto.ApiResponse;
import com.modoria.domain.order.dto.response.OrderResponse;
import com.modoria.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for order endpoints.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order API")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Received request to create order");
        OrderResponse response = orderService.create(request);
        log.debug("Order created with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Order created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Long id) {
        log.debug("Fetching order request: {}", id);
        OrderResponse response = orderService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<ApiResponse<OrderResponse>> getByOrderNumber(@PathVariable String orderNumber) {
        OrderResponse response = orderService.getByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getCurrentUserOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> response = orderService.getCurrentUserOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders (Admin)")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> response = orderService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by user ID (Admin)")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> response = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by status (Admin)")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getByStatus(
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> response = orderService.getByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (Admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        log.info("Admin request to update order {} status to {}", id, status);
        OrderResponse response = orderService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Order status updated successfully"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable Long id) {
        log.info("Request to cancel order: {}", id);
        OrderResponse response = orderService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Order cancelled successfully"));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark order as paid (Admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> markAsPaid(
            @PathVariable Long id,
            @RequestParam String paymentId) {
        OrderResponse response = orderService.markAsPaid(id, paymentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order marked as paid successfully"));
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark order as shipped (Admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> markAsShipped(
            @PathVariable Long id,
            @RequestParam String trackingNumber) {
        OrderResponse response = orderService.markAsShipped(id, trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "Order marked as shipped successfully"));
    }

    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark order as delivered (Admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> markAsDelivered(@PathVariable Long id) {
        OrderResponse response = orderService.markAsDelivered(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Order marked as delivered successfully"));
    }
}
