package com.scaler.store.payment.repository;

import com.scaler.store.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByOrderNumber(String orderNumber);
    Optional<PaymentTransaction> findByReceiptNumber(String receiptNumber);
    Optional<PaymentTransaction> findByTransactionReference(String transactionReference);
}
