package com.scaler.store.cart.service;

import com.scaler.store.cart.dto.AddToCartRequest;
import com.scaler.store.cart.dto.UpdateCartItemRequest;
import com.scaler.store.cart.entity.Cart;
import com.scaler.store.cart.entity.CartItem;
import com.scaler.store.cart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private static final String CART_CACHE_PREFIX = "cart:";

    private final CartRepository cartRepository;
    private final ConcurrentHashMap<Long, Cart> inMemoryCarts = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getCart(Long userId) {
        String cacheKey = CART_CACHE_PREFIX + userId;

        // Try Redis cache first
        if (redisTemplate != null) {
            try {
                Object cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached instanceof Cart) {
                    log.info("Retrieved cart for user {} from Redis Cache (Sub-millisecond hit)", userId);
                    return (Cart) cached;
                }
            } catch (Exception e) {
                log.warn("Redis cart cache read error: {}", e.getMessage());
            }
        }

        // Fetch from MongoDB with resilient in-memory fallback
        Cart cart = fetchCartFromStore(userId);

        // Store in Redis
        cacheCart(userId, cart);
        return cart;
    }

    public Cart addItem(Long userId, AddToCartRequest req) {
        Cart cart = getCart(userId);

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(req.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + req.getQuantity());
            item.recalculateSubtotal();
        } else {
            CartItem newItem = new CartItem(
                    req.getProductId(),
                    req.getProductName(),
                    req.getProductSku(),
                    req.getUnitPrice(),
                    req.getQuantity()
            );
            cart.getItems().add(newItem);
        }

        cart.recalculateTotals();
        Cart saved = saveCartToStore(cart);
        cacheCart(userId, saved);
        return saved;
    }

    public Cart updateItem(Long userId, UpdateCartItemRequest req) {
        Cart cart = getCart(userId);

        if (req.getQuantity() <= 0) {
            cart.getItems().removeIf(i -> i.getProductId().equals(req.getProductId()));
        } else {
            for (CartItem item : cart.getItems()) {
                if (item.getProductId().equals(req.getProductId())) {
                    item.setQuantity(req.getQuantity());
                    item.recalculateSubtotal();
                    break;
                }
            }
        }

        cart.recalculateTotals();
        Cart saved = saveCartToStore(cart);
        cacheCart(userId, saved);
        return saved;
    }

    public Cart removeItem(Long userId, Long productId) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        cart.recalculateTotals();
        Cart saved = saveCartToStore(cart);
        cacheCart(userId, saved);
        return saved;
    }

    public void clearCart(Long userId) {
        deleteCartFromStore(userId);
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(CART_CACHE_PREFIX + userId);
            } catch (Exception e) {
                log.warn("Failed to evict cart from Redis: {}", e.getMessage());
            }
        }
    }

    private Cart fetchCartFromStore(Long userId) {
        try {
            return cartRepository.findByUserId(userId).orElseGet(() -> saveCartToStore(new Cart(userId)));
        } catch (Exception e) {
            log.warn("MongoDB unavailable ({}), utilizing thread-safe in-memory cart store.", e.getMessage());
            return inMemoryCarts.computeIfAbsent(userId, Cart::new);
        }
    }

    private Cart saveCartToStore(Cart cart) {
        inMemoryCarts.put(cart.getUserId(), cart);
        try {
            return cartRepository.save(cart);
        } catch (Exception e) {
            log.warn("MongoDB unavailable ({}), saved to in-memory store only.", e.getMessage());
            return cart;
        }
    }

    private void deleteCartFromStore(Long userId) {
        inMemoryCarts.remove(userId);
        try {
            cartRepository.deleteByUserId(userId);
        } catch (Exception e) {
            log.warn("MongoDB unavailable ({}), removed from in-memory store.", e.getMessage());
        }
    }

    private void cacheCart(Long userId, Cart cart) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(CART_CACHE_PREFIX + userId, cart, Duration.ofMinutes(30));
            } catch (Exception e) {
                log.warn("Redis cart cache write error: {}", e.getMessage());
            }
        }
    }
}
