package com.scaler.store.payment.service;

import com.scaler.store.common.events.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderPlacedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedKafkaConsumer.class);

    private final PaymentService paymentService;

    public OrderPlacedKafkaConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-events:order-events}", groupId = "payment-service-group")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Payment Service consumed OrderPlacedEvent for OrderNumber: {}, TotalAmount: {}",
                event.getOrderNumber(), event.getTotalAmount());

        try {
            paymentService.processPayment(
                    event.getOrderId(),
                    event.getOrderNumber(),
                    event.getUserId(),
                    event.getUserEmail(),
                    event.getTotalAmount(),
                    event.getPaymentMethod()
            );
        } catch (Exception e) {
            log.error("Failed to process payment automatically for OrderNumber: {}: {}",
                    event.getOrderNumber(), e.getMessage());
        }
    }
}
