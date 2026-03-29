package com.scaler.store.order.service;

import com.scaler.store.common.events.OrderPlacedEvent;
import com.scaler.store.common.models.AddressDto;
import com.scaler.store.common.models.OrderItemDto;
import com.scaler.store.common.models.OrderStatus;
import com.scaler.store.order.dto.CheckoutRequest;
import com.scaler.store.order.dto.OrderResponse;
import com.scaler.store.order.entity.Order;
import com.scaler.store.order.entity.OrderItem;
import com.scaler.store.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderKafkaProducer kafkaProducer;

    public OrderService(OrderRepository orderRepository, OrderKafkaProducer kafkaProducer) {
        this.orderRepository = orderRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public OrderResponse createOrder(Long userId, String userEmail, CheckoutRequest req) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String orderNumber = "ORD-" + timestamp + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setUserEmail(userEmail);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentMethod(req.getPaymentMethod());

        AddressDto addr = req.getShippingAddress();
        if (addr != null) {
            order.setShippingStreet(addr.getStreet());
            order.setShippingCity(addr.getCity());
            order.setShippingState(addr.getState());
            order.setShippingPostalCode(addr.getPostalCode());
            order.setShippingCountry(addr.getCountry());
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItemDto> itemDtos = new ArrayList<>();

        for (OrderItemDto itemReq : req.getItems()) {
            OrderItem item = new OrderItem(
                    itemReq.getProductId(),
                    itemReq.getProductName(),
                    itemReq.getProductSku(),
                    itemReq.getQuantity(),
                    itemReq.getUnitPrice()
            );
            order.addItem(item);
            total = total.add(item.getSubtotal());

            itemDtos.add(new OrderItemDto(
                    item.getProductId(),
                    item.getProductName(),
                    item.getProductSku(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
            ));
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        // Emit OrderPlacedEvent via Kafka
        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getUserId(),
                savedOrder.getUserEmail(),
                savedOrder.getTotalAmount(),
                savedOrder.getPaymentMethod(),
                addr,
                itemDtos,
                savedOrder.getStatus()
        );
        kafkaProducer.publishOrderPlaced(event);

        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
        return mapToResponse(order);
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with Number: " + orderNumber));
        return mapToResponse(order);
    }

    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::mapToResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        order.setStatus(status);
        Order updated = orderRepository.save(order);
        return mapToResponse(updated);
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse res = new OrderResponse();
        res.setId(order.getId());
        res.setOrderNumber(order.getOrderNumber());
        res.setUserId(order.getUserId());
        res.setUserEmail(order.getUserEmail());
        res.setStatus(order.getStatus());
        res.setPaymentMethod(order.getPaymentMethod());
        res.setTotalAmount(order.getTotalAmount());

        AddressDto addr = new AddressDto(
                order.getShippingStreet(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPostalCode(),
                order.getShippingCountry()
        );
        res.setShippingAddress(addr);

        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(i -> new OrderItemDto(i.getProductId(), i.getProductName(), i.getProductSku(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                .collect(Collectors.toList());
        res.setItems(itemDtos);

        res.setCreatedAt(order.getCreatedAt());
        res.setUpdatedAt(order.getUpdatedAt());
        return res;
    }
}
