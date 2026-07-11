package com.scaler.store.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.store.common.events.NotificationEvent;
import com.scaler.store.common.events.OrderPlacedEvent;
import com.scaler.store.common.events.PasswordResetEvent;
import com.scaler.store.common.events.PaymentStatusEvent;
import com.scaler.store.common.events.UserRegisteredEvent;
import com.scaler.store.common.models.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationKafkaConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.user-events:user-events}", groupId = "notification-service-group")
    public void handleUserEvent(Object rawEvent) {
        try {
            log.info("Notification Service received event on user-events topic: {}", rawEvent);
            String json = objectMapper.writeValueAsString(rawEvent);

            if (json.contains("resetToken")) {
                PasswordResetEvent reset = objectMapper.readValue(json, PasswordResetEvent.class);
                String subject = "Scaler Store: Password Reset Request";
                String body = "Hello,\n\nYou requested a password reset. Click here to reset: "
                        + reset.getResetUrl() + "\nToken expires at: " + reset.getExpiryTime();
                notificationService.sendEmail(reset.getEmail(), NotificationType.PASSWORD_RESET, subject, body);
            } else {
                UserRegisteredEvent reg = objectMapper.readValue(json, UserRegisteredEvent.class);
                String subject = "Welcome to Scaler Store, " + reg.getFullName() + "!";
                String body = "Dear " + reg.getFullName() + ",\n\nWelcome to Scaler Store! Your account with "
                        + reg.getEmail() + " is now active. Start browsing our curated catalog today!";
                notificationService.sendEmail(reg.getEmail(), NotificationType.WELCOME_EMAIL, subject, body);
            }
        } catch (Exception e) {
            log.error("Error processing user-events notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.order-events:order-events}", groupId = "notification-service-group")
    public void handleOrderEvent(Object rawEvent) {
        try {
            log.info("Notification Service received event on order-events topic: {}", rawEvent);
            String json = objectMapper.writeValueAsString(rawEvent);
            OrderPlacedEvent event = objectMapper.readValue(json, OrderPlacedEvent.class);

            String subject = "Order Confirmation - " + event.getOrderNumber();
            String body = "Thank you for your order!\n\nOrder Number: " + event.getOrderNumber()
                    + "\nTotal Amount: $" + event.getTotalAmount()
                    + "\nPayment Method: " + event.getPaymentMethod()
                    + "\nStatus: " + event.getStatus()
                    + "\nItems count: " + (event.getItems() != null ? event.getItems().size() : 0);

            notificationService.sendEmail(event.getUserEmail(), NotificationType.ORDER_CONFIRMATION, subject, body);
        } catch (Exception e) {
            log.error("Error processing order-events notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-events:payment-events}", groupId = "notification-service-group")
    public void handlePaymentEvent(Object rawEvent) {
        try {
            log.info("Notification Service received event on payment-events topic: {}", rawEvent);
            String json = objectMapper.writeValueAsString(rawEvent);
            PaymentStatusEvent event = objectMapper.readValue(json, PaymentStatusEvent.class);

            String subject = "Payment Receipt for Order " + event.getOrderNumber();
            String body = "Payment Status: " + event.getStatus()
                    + "\nOrder Number: " + event.getOrderNumber()
                    + "\nTransaction Reference: " + event.getTransactionReference()
                    + "\nAmount Paid: $" + event.getAmount()
                    + "\nPayment Method: " + event.getPaymentMethod();

            notificationService.sendEmail(event.getUserEmail(), NotificationType.PAYMENT_RECEIPT, subject, body);
        } catch (Exception e) {
            log.error("Error processing payment-events notification: {}", e.getMessage());
        }
    }
}
