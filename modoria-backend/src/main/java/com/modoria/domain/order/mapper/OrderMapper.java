package com.modoria.domain.order.mapper;

import com.modoria.domain.order.entity.Order;
import com.modoria.domain.order.entity.OrderItem;
import com.modoria.domain.order.dto.response.OrderItemResponse;
import com.modoria.domain.order.dto.response.OrderResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Simple mapper for Order entity.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "shippingFirstName", source = "shippingAddress.firstName")
    @Mapping(target = "shippingLastName", source = "shippingAddress.lastName")
    @Mapping(target = "shippingAddressLine1", source = "shippingAddress.addressLine1")
    @Mapping(target = "shippingAddressLine2", source = "shippingAddress.addressLine2")
    @Mapping(target = "shippingCity", source = "shippingAddress.city")
    @Mapping(target = "shippingState", source = "shippingAddress.state")
    @Mapping(target = "shippingPostalCode", source = "shippingAddress.postalCode")
    @Mapping(target = "shippingCountry", source = "shippingAddress.country")
    @Mapping(target = "shippingPhone", source = "shippingAddress.phone")
    @Mapping(target = "trackingNumber", source = "shippingAddress.trackingNumber")
    @Mapping(target = "shippedAt", source = "shippingAddress.shippedAt")
    @Mapping(target = "deliveredAt", source = "shippingAddress.deliveredAt")
    @Mapping(target = "paymentMethod", source = "payment.paymentProvider")
    @Mapping(target = "paymentStatus", source = "payment.status")
    @Mapping(target = "paidAt", source = "payment.paidAt")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    OrderItemResponse toItemResponse(OrderItem orderItem);

    List<OrderResponse> toResponseList(List<Order> orders);

    @AfterMapping
    default void calculateOrderFields(@MappingTarget OrderResponse response, Order order) {
        if (order.getUser() != null) {
            response.setUserName(order.getUser().getFullName());
        }
    }
}


