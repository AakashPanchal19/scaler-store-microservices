package com.scaler.store.cart.controller;

import com.scaler.store.cart.dto.AddToCartRequest;
import com.scaler.store.cart.dto.UpdateCartItemRequest;
import com.scaler.store.cart.entity.Cart;
import com.scaler.store.cart.service.CartService;
import com.scaler.store.common.models.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private Long resolveUserId(String headerUserId, Long paramUserId) {
        if (headerUserId != null && !headerUserId.isBlank()) {
            return Long.parseLong(headerUserId);
        }
        return paramUserId;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Cart>> getCart(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID not provided"));
        }
        Cart cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.ok("Cart retrieved successfully", cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Cart>> addItem(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @Valid @RequestBody AddToCartRequest request) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID not provided"));
        }
        Cart cart = cartService.addItem(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart", cart));
    }

    @PutMapping("/items")
    public ResponseEntity<ApiResponse<Cart>> updateItem(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID not provided"));
        }
        Cart cart = cartService.updateItem(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Cart item updated", cart));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Cart>> removeItem(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @PathVariable("productId") Long productId) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID not provided"));
        }
        Cart cart = cartService.removeItem(userId, productId);
        return ResponseEntity.ok(ApiResponse.ok("Item removed from cart", cart));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID not provided"));
        }
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared successfully", null));
    }
}
