package com.scaler.store.order.service;

import com.scaler.store.common.events.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.order-events:order-events}")
    private String orderEventsTopic;

    public OrderKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        CompletableFuture.runAsync(() -> {
            log.info("Emitting OrderPlacedEvent to Kafka topic '{}' for OrderNumber: {}", orderEventsTopic, event.getOrderNumber());
            try {
                kafkaTemplate.send(orderEventsTopic, event.getOrderNumber(), event);
            } catch (Exception e) {
                log.warn("Kafka broker offline - skipping async OrderPlacedEvent dispatch: {}", e.getMessage());
            }
        });
    }
}
