package com.scaler.store.common.events;

import com.scaler.store.common.models.AddressDto;
import com.scaler.store.common.models.OrderItemDto;
import com.scaler.store.common.models.OrderStatus;
import com.scaler.store.common.models.PaymentMethod;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderPlacedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private AddressDto shippingAddress;
    private List<OrderItemDto> items;
    private OrderStatus status;
    private LocalDateTime timestamp;

    public OrderPlacedEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public OrderPlacedEvent(Long orderId, String orderNumber, Long userId, String userEmail,
                            BigDecimal totalAmount, PaymentMethod paymentMethod,
                            AddressDto shippingAddress, List<OrderItemDto> items, OrderStatus status) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.userEmail = userEmail;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public AddressDto getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(AddressDto shippingAddress) { this.shippingAddress = shippingAddress; }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
