package com.scaler.store.payment.service;

import com.scaler.store.common.events.PaymentStatusEvent;
import com.scaler.store.common.models.PaymentMethod;
import com.scaler.store.common.models.PaymentStatus;
import com.scaler.store.payment.dto.PaymentProcessRequest;
import com.scaler.store.payment.dto.PaymentReceiptDto;
import com.scaler.store.payment.entity.PaymentTransaction;
import com.scaler.store.payment.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentTransactionRepository paymentRepository;
    private final PaymentKafkaProducer kafkaProducer;

    public PaymentService(PaymentTransactionRepository paymentRepository, PaymentKafkaProducer kafkaProducer) {
        this.paymentRepository = paymentRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public PaymentReceiptDto processPayment(Long orderId, String orderNumber, Long userId, String userEmail,
                                           BigDecimal amount, PaymentMethod method) {
        log.info("Initiating payment processing for OrderNumber: {}, Amount: {}, Method: {}", orderNumber, amount, method);

        String txnRef = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String receiptNumber = "RCP-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        PaymentTransaction txn = new PaymentTransaction();
        txn.setOrderId(orderId);
        txn.setOrderNumber(orderNumber);
        txn.setUserId(userId != null ? userId : 0L);
        txn.setUserEmail(userEmail);
        txn.setAmount(amount);
        txn.setPaymentMethod(method);
        txn.setTransactionReference(txnRef);
        txn.setReceiptNumber(receiptNumber);

        // Simulated secure gateway authorization logic
        // Any valid amount > 0 succeeds
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            txn.setStatus(PaymentStatus.SUCCESS);
            txn.setFailureReason(null);
        } else {
            txn.setStatus(PaymentStatus.FAILED);
            txn.setFailureReason("Invalid transaction amount");
        }

        PaymentTransaction saved = paymentRepository.save(txn);

        // Emit PaymentStatusEvent to Kafka
        PaymentStatusEvent event = new PaymentStatusEvent(
                saved.getId(),
                saved.getOrderId(),
                saved.getOrderNumber(),
                saved.getUserId(),
                saved.getUserEmail(),
                saved.getAmount(),
                saved.getPaymentMethod(),
                saved.getStatus(),
                saved.getTransactionReference(),
                saved.getFailureReason()
        );
        kafkaProducer.publishPaymentStatus(event);

        return mapToReceipt(saved);
    }

    @Transactional
    public PaymentReceiptDto processPaymentDirect(Long userId, String userEmail, PaymentProcessRequest req) {
        return processPayment(req.getOrderId(), req.getOrderNumber(), userId, userEmail, req.getAmount(), req.getPaymentMethod());
    }

    public PaymentReceiptDto getReceiptByPaymentId(Long paymentId) {
        PaymentTransaction txn = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));
        return mapToReceipt(txn);
    }

    public PaymentReceiptDto getReceiptByOrderNumber(String orderNumber) {
        PaymentTransaction txn = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderNumber));
        return mapToReceipt(txn);
    }

    private PaymentReceiptDto mapToReceipt(PaymentTransaction txn) {
        PaymentReceiptDto receipt = new PaymentReceiptDto();
        receipt.setPaymentId(txn.getId());
        receipt.setReceiptNumber(txn.getReceiptNumber());
        receipt.setTransactionReference(txn.getTransactionReference());
        receipt.setOrderId(txn.getOrderId());
        receipt.setOrderNumber(txn.getOrderNumber());
        receipt.setAmount(txn.getAmount());
        receipt.setPaymentMethod(txn.getPaymentMethod());
        receipt.setStatus(txn.getStatus());
        receipt.setPaymentDate(txn.getCreatedAt());
        return receipt;
    }
}
