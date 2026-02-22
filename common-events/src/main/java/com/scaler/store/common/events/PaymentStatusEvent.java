package com.scaler.store.common.events;

import com.scaler.store.common.models.PaymentMethod;
import com.scaler.store.common.models.PaymentStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentStatusEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long paymentId;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionReference;
    private String failureReason;
    private LocalDateTime timestamp;

    public PaymentStatusEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public PaymentStatusEvent(Long paymentId, Long orderId, String orderNumber, Long userId,
                              String userEmail, BigDecimal amount, PaymentMethod paymentMethod,
                              PaymentStatus status, String transactionReference, String failureReason) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.userEmail = userEmail;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionReference = transactionReference;
        this.failureReason = failureReason;
        this.timestamp = LocalDateTime.now();
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
