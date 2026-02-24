package com.modoria.domain.order.service.impl;

import com.modoria.domain.order.enums.OrderStatus;
import com.modoria.domain.order.dto.request.CreateOrderRequest;
import com.modoria.domain.order.dto.request.ShippingAddressRequest;
import com.modoria.domain.order.dto.response.OrderResponse;
import com.modoria.domain.order.entity.Order;
import com.modoria.domain.order.entity.OrderItem;
import com.modoria.domain.order.entity.ShippingAddress;
import com.modoria.infrastructure.exceptions.business.CartEmptyException;
import com.modoria.infrastructure.exceptions.business.InsufficientStockException;
import com.modoria.infrastructure.exceptions.business.OrderProcessingException;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.domain.order.mapper.OrderMapper;
import com.modoria.domain.cart.entity.Cart;
import com.modoria.domain.cart.entity.CartItem;
import com.modoria.domain.cart.repository.CartRepository;
import com.modoria.domain.order.repository.OrderRepository;
import com.modoria.domain.product.entity.Product;
import com.modoria.domain.product.entity.ProductVariant;
import com.modoria.domain.product.repository.ProductRepository;
import com.modoria.domain.user.entity.User;
import com.modoria.domain.user.repository.UserRepository;
import com.modoria.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final com.modoria.domain.user.repository.AddressRepository addressRepository;
    private final com.modoria.domain.coupon.repository.CouponRepository couponRepository;
    private final OrderMapper orderMapper;
    private final com.modoria.infrastructure.mail.EmailService emailService;
    private final com.modoria.domain.notification.service.NotificationService notificationService;

    @Override
    public OrderResponse create(CreateOrderRequest request) {
        log.info("Creating order for user");
        User user = getCurrentUser();
        log.debug("Found user: {}", user.getEmail());
        Cart cart = cartRepository.findByUserIdWithItems(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", user.getId()));

        if (cart.getItems().isEmpty()) {
            throw new CartEmptyException("Cannot place an order with an empty cart");
        }

        // Validate stock
        validateStock(cart.getItems());

        BigDecimal subtotal = cart.getTotalAmount();
        BigDecimal discount = BigDecimal.ZERO;
        String appliedCoupon = null;

        // Apply coupon if provided
        if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
            com.modoria.domain.coupon.entity.Coupon coupon = couponRepository
                    .findByCodeAndIsActiveTrue(request.getCouponCode())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code"));

            if (!coupon.isValid()) {
                throw new IllegalArgumentException("Coupon is expired or has reached usage limit");
            }

            if (coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                throw new IllegalArgumentException("Order total does not meet minimum amount for this coupon");
            }

            discount = coupon.calculateDiscount(subtotal);
            appliedCoupon = coupon.getCode();

            // Increment usage count
            coupon.setUsageCount(coupon.getUsageCount() + 1);
            couponRepository.save(coupon);
        }

        BigDecimal totalAmount = subtotal.subtract(discount);

        // Create Order
        Order order = Order.builder()
                .user(user)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .discountAmount(discount)
                .appliedCouponCode(appliedCoupon)
                .totalAmount(totalAmount)
                .currency(cart.getCurrency())
                .customerNotes(request.getCustomerNotes())
                .build();

        // Handle Shipping Address
        ShippingAddress shippingAddress;
        if (request.getAddressId() != null) {
            com.modoria.domain.user.entity.Address userAddress = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));

            if (!userAddress.getUser().getId().equals(user.getId())) {
                throw new com.modoria.infrastructure.exceptions.auth.UnauthorizedException(
                        "Address does not belong to user");
            }

            shippingAddress = ShippingAddress.builder()
                    .firstName(userAddress.getFirstName())
                    .lastName(userAddress.getLastName())
                    .addressLine1(userAddress.getAddressLine1())
                    .addressLine2(userAddress.getAddressLine2())
                    .city(userAddress.getCity())
                    .state(userAddress.getState())
                    .postalCode(userAddress.getPostalCode())
                    .country(userAddress.getCountry())
                    .phone(userAddress.getPhone())
                    .order(order)
                    .build();
        } else if (request.getShippingAddress() != null) {
            ShippingAddressRequest shipReq = request.getShippingAddress();
            shippingAddress = ShippingAddress.builder()
                    .firstName(shipReq.getFirstName())
                    .lastName(shipReq.getLastName())
                    .addressLine1(shipReq.getAddressLine1())
                    .addressLine2(shipReq.getAddressLine2())
                    .city(shipReq.getCity())
                    .state(shipReq.getState())
                    .postalCode(shipReq.getPostalCode())
                    .country(shipReq.getCountry())
                    .phone(shipReq.getPhone())
                    .order(order)
                    .build();
        } else {
            throw new IllegalArgumentException("Either addressId or shippingAddress must be provided");
        }
        order.setShippingAddress(shippingAddress);

        // Create Order Items and Deduct Stock
        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            ProductVariant variant = cartItem.getProductVariant();

            // Deduct stock
            if (variant != null) {
                if (variant.getInventoryQuantity() < cartItem.getQuantity()) {
                    throw new InsufficientStockException("Not enough stock for variant: " + variant.getSku());
                }
                variant.setInventoryQuantity(variant.getInventoryQuantity() - cartItem.getQuantity());
                // Also deduct from total product quantity to keep it in sync
                product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            } else {
                if (product.getQuantity() < cartItem.getQuantity()) {
                    throw new InsufficientStockException("Not enough stock for product: " + product.getName());
                }
                product.setQuantity(product.getQuantity() - cartItem.getQuantity());

                // Check for low stock alert
                if (product.isLowStock()) {
                    notificationService.notifyRole("ROLE_ADMIN",
                            "Low Stock Alert",
                            "Product '" + product.getName() + "' is low in stock (" + product.getQuantity() + " left).",
                            com.modoria.domain.notification.enums.NotificationType.LOW_STOCK_ALERT,
                            "/admin/inventory",
                            "{\"productId\":" + product.getId() + "}");
                }
            }
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productVariant(variant)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(variant != null && variant.getPrice() != null ? variant.getPrice() : product.getPrice())
                    .build();
            orderItems.add(orderItem);
        }
        order.setItems(new java.util.ArrayList<>(orderItems));

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with number: {}", savedOrder.getOrderNumber());

        // Notify Customer
        notificationService.sendNotification(user,
                "Order Placed",
                "Your order #" + savedOrder.getOrderNumber() + " has been placed successfully.",
                com.modoria.domain.notification.enums.NotificationType.ORDER_STATUS_UPDATE,
                "/orders/" + savedOrder.getId(),
                "{\"orderId\":" + savedOrder.getId() + "}");

        // Clear Cart
        cart.clearItems();
        cartRepository.save(cart);

        cartRepository.save(cart);

        // Send Confirmation Email
        try {
            emailService.sendOrderConfirmation(user.getEmail(), savedOrder.getId(), savedOrder.getOrderNumber());
            log.debug("Order confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}", savedOrder.getOrderNumber(),
                    e.getMessage());
        }

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        log.debug("Fetching order by id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        // Security check: ensure user owns order or is admin (omitted for brevity,
        // handled by Service/Security usually)
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "number", orderNumber));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getCurrentUserOrders(Pageable pageable) {
        User user = getCurrentUser();
        return orderRepository.findByUserId(user.getId(), pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAll(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        log.info("Updating order {} status to: {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        log.info("Order {} status changed from {} to {}", savedOrder.getOrderNumber(), previousStatus, status);

        // Notify Customer of status update
        notificationService.sendNotification(savedOrder.getUser(),
                "Order Status Update",
                "Your order #" + savedOrder.getOrderNumber() + " is now " + status.name(),
                com.modoria.domain.notification.enums.NotificationType.ORDER_STATUS_UPDATE,
                "/orders/" + savedOrder.getId(),
                "{\"orderId\":" + savedOrder.getId() + "}");

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public OrderResponse cancel(Long id) {
        log.info("Cancelling order: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            log.warn("Cannot cancel order {} - already shipped/delivered", order.getOrderNumber());
            throw new OrderProcessingException("Cannot cancel order that has already been shipped or delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            ProductVariant variant = item.getProductVariant();

            if (variant != null) {
                variant.setInventoryQuantity(variant.getInventoryQuantity() + item.getQuantity());
                product.setQuantity(product.getQuantity() + item.getQuantity());
                log.debug("Restored {} units to variant {}", item.getQuantity(), variant.getSku());
            } else {
                product.setQuantity(product.getQuantity() + item.getQuantity());
                log.debug("Restored {} units to product {}", item.getQuantity(), product.getName());
            }
            productRepository.save(product);
        }

        log.info("Order {} cancelled successfully", order.getOrderNumber());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse markAsPaid(Long id, String paymentId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        order.setStatus(OrderStatus.PAID);
        // In a real implementation, we would save payment details here via
        // PaymentRepository
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse markAsShipped(Long id, String trackingNumber) {
        log.info("Marking order {} as shipped with tracking: {}", id, trackingNumber);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Validate order is paid before shipping
        if (order.getStatus() != OrderStatus.PAID) {
            log.warn("Cannot ship order {} - not paid. Status: {}", order.getOrderNumber(), order.getStatus());
            throw new OrderProcessingException(
                    "Order must be paid before shipping. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setTrackingNumber(trackingNumber);

        // Update shipping address with tracking info and timestamp
        if (order.getShippingAddress() != null) {
            order.getShippingAddress().setTrackingNumber(trackingNumber);
            order.getShippingAddress().setShippedAt(java.time.LocalDateTime.now());
        }

        // Send Shipping Update Email
        if (order.getUser() != null) {
            emailService.sendShippingUpdate(order.getUser().getEmail(), order.getOrderNumber(), trackingNumber);
            log.debug("Shipping update email sent for order: {}", order.getOrderNumber());
        }

        log.info("Order {} marked as shipped", order.getOrderNumber());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse markAsDelivered(Long id) {
        log.info("Marking order {} as delivered", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Validate order is shipped before delivery
        if (order.getStatus() != OrderStatus.SHIPPED) {
            log.warn("Cannot mark order {} as delivered - not shipped. Status: {}", order.getOrderNumber(),
                    order.getStatus());
            throw new OrderProcessingException(
                    "Order must be shipped before marking as delivered. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.DELIVERED);

        // Update shipping address with delivered timestamp
        if (order.getShippingAddress() != null) {
            order.getShippingAddress().setDeliveredAt(java.time.LocalDateTime.now());
        }

        log.info("Order {} marked as delivered", order.getOrderNumber());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void validateStock(List<CartItem> items) {
        for (CartItem item : items) {
            if (item.getProduct().getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for product: " + item.getProduct().getName());
            }
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
