package com.scaler.store.order.service;

import com.scaler.store.common.events.PaymentStatusEvent;
import com.scaler.store.common.models.OrderStatus;
import com.scaler.store.common.models.PaymentStatus;
import com.scaler.store.order.entity.Order;
import com.scaler.store.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderRepository orderRepository;

    public PaymentEventConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-events:payment-events}", groupId = "order-service-group")
    @Transactional
    public void handlePaymentStatusEvent(PaymentStatusEvent event) {
        log.info("Received PaymentStatusEvent for OrderNumber: {}, Status: {}", event.getOrderNumber(), event.getStatus());

        orderRepository.findByOrderNumber(event.getOrderNumber()).ifPresentOrElse(order -> {
            if (event.getStatus() == PaymentStatus.SUCCESS) {
                order.setStatus(OrderStatus.CONFIRMED);
                log.info("Order {} status updated to CONFIRMED following successful payment.", order.getOrderNumber());
            } else {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                log.warn("Order {} status updated to PAYMENT_FAILED due to payment failure.", order.getOrderNumber());
            }
            orderRepository.save(order);
        }, () -> log.error("Order not found for PaymentStatusEvent OrderNumber: {}", event.getOrderNumber()));
    }
}
