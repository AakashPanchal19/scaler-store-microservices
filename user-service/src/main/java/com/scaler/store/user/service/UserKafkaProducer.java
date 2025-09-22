package com.scaler.store.user.service;

import com.scaler.store.common.events.PasswordResetEvent;
import com.scaler.store.common.events.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UserKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(UserKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.user-events:user-events}")
    private String userEventsTopic;

    public UserKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegistered(UserRegisteredEvent event) {
        CompletableFuture.runAsync(() -> {
            log.info("Publishing UserRegisteredEvent for email: {}", event.getEmail());
            try {
                kafkaTemplate.send(userEventsTopic, String.valueOf(event.getUserId()), event);
            } catch (Exception e) {
                log.warn("Kafka broker offline - skipping async UserRegisteredEvent dispatch: {}", e.getMessage());
            }
        });
    }

    public void publishPasswordReset(PasswordResetEvent event) {
        CompletableFuture.runAsync(() -> {
            log.info("Publishing PasswordResetEvent for email: {}", event.getEmail());
            try {
                kafkaTemplate.send(userEventsTopic, String.valueOf(event.getUserId()), event);
            } catch (Exception e) {
                log.warn("Kafka broker offline - skipping async PasswordResetEvent dispatch: {}", e.getMessage());
            }
        });
    }
}

