package com.scaler.store.payment.service;

import com.scaler.store.common.events.PaymentStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PaymentKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.payment-events:payment-events}")
    private String paymentEventsTopic;

    public PaymentKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentStatus(PaymentStatusEvent event) {
        CompletableFuture.runAsync(() -> {
            log.info("Emitting PaymentStatusEvent to Kafka topic '{}' for OrderNumber: {}, Status: {}",
                    paymentEventsTopic, event.getOrderNumber(), event.getStatus());
            try {
                kafkaTemplate.send(paymentEventsTopic, event.getOrderNumber(), event);
            } catch (Exception e) {
                log.warn("Kafka broker offline - skipping async PaymentStatusEvent dispatch: {}", e.getMessage());
            }
        });
    }
}
