package com.scaler.store.order.controller;

import com.scaler.store.common.models.ApiResponse;
import com.scaler.store.common.models.OrderStatus;
import com.scaler.store.order.dto.CheckoutRequest;
import com.scaler.store.order.dto.OrderResponse;
import com.scaler.store.order.dto.OrderStatusUpdateDto;
import com.scaler.store.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Long resolveUserId(String headerUserId, Long paramUserId) {
        if (headerUserId != null && !headerUserId.isBlank()) {
            return Long.parseLong(headerUserId);
        }
        return paramUserId;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = "X-User-Email", required = false) String headerUserEmail,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestParam(value = "email", required = false) String paramEmail,
            @Valid @RequestBody CheckoutRequest request) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        String userEmail = headerUserEmail != null ? headerUserEmail : paramEmail;

        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID not provided"));
        }

        try {
            OrderResponse response = orderService.createOrder(userId, userEmail, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Order placed successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID not provided"));
        }
        Page<OrderResponse> orders = orderService.getUserOrders(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable("id") Long id) {
        try {
            OrderResponse order = orderService.getOrderById(id);
            return ResponseEntity.ok(ApiResponse.ok(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/track/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(@PathVariable("orderNumber") String orderNumber) {
        try {
            OrderResponse order = orderService.getOrderByNumber(orderNumber);
            return ResponseEntity.ok(ApiResponse.ok("Order status tracking retrieved", order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody OrderStatusUpdateDto dto) {
        try {
            OrderResponse updated = orderService.updateOrderStatus(id, dto.getStatus());
            return ResponseEntity.ok(ApiResponse.ok("Order status updated", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
