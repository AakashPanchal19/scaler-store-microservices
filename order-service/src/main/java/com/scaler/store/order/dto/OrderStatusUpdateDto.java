package com.scaler.store.order.dto;

import com.scaler.store.common.models.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class OrderStatusUpdateDto {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    public OrderStatusUpdateDto() {}

    public OrderStatusUpdateDto(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
