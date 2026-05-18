package com.scaler.store.payment.controller;

import com.scaler.store.common.models.ApiResponse;
import com.scaler.store.payment.dto.PaymentProcessRequest;
import com.scaler.store.payment.dto.PaymentReceiptDto;
import com.scaler.store.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    private Long resolveUserId(String headerUserId, Long paramUserId) {
        if (headerUserId != null && !headerUserId.isBlank()) {
            return Long.parseLong(headerUserId);
        }
        return paramUserId;
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentReceiptDto>> processPayment(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = "X-User-Email", required = false) String headerUserEmail,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestParam(value = "email", required = false) String paramEmail,
            @Valid @RequestBody PaymentProcessRequest request) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        String userEmail = headerUserEmail != null ? headerUserEmail : paramEmail;

        try {
            PaymentReceiptDto receipt = paymentService.processPaymentDirect(userId, userEmail, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Payment processed successfully", receipt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/receipt/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentReceiptDto>> getReceipt(@PathVariable("paymentId") Long paymentId) {
        try {
            PaymentReceiptDto receipt = paymentService.getReceiptByPaymentId(paymentId);
            return ResponseEntity.ok(ApiResponse.ok("Payment receipt retrieved", receipt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<ApiResponse<PaymentReceiptDto>> getPaymentByOrder(@PathVariable("orderNumber") String orderNumber) {
        try {
            PaymentReceiptDto receipt = paymentService.getReceiptByOrderNumber(orderNumber);
            return ResponseEntity.ok(ApiResponse.ok("Payment transaction retrieved", receipt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
