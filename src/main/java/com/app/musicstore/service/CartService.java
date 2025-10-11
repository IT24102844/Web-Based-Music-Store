package com.app.musicstore.service;

import com.app.musicstore.model.CartItem;
import com.app.musicstore.model.Product;
import com.app.musicstore.repository.CartItemRepository;
import com.app.musicstore.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    public void addToCart(Long userId, Long productId, int quantity) {
        var existing = cartItemRepository.findByUserIdAndProduct_Id(userId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + Math.max(1, quantity));
            cartItemRepository.save(item);
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProduct(product);
            item.setQuantity(Math.max(1, quantity));
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void updateQuantities(Long userId, List<Long> productIds, List<Integer> quantities) {
        for (int i = 0; i < productIds.size(); i++) {
            Long pid = productIds.get(i);
            int qty = Math.max(1, quantities.get(i));
            var existing = cartItemRepository.findByUserIdAndProduct_Id(userId, pid)
                    .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
            existing.setQuantity(qty);
            cartItemRepository.save(existing);
        }
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}


