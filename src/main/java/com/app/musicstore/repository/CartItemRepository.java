package com.app.musicstore.repository;

import com.app.musicstore.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndProduct_Id(Long userId, Long productId);

    @Transactional
    @Modifying
    @Query("delete from CartItem c where c.userId = :userId and c.product.id = :productId")
    void deleteByUserIdAndProductId(Long userId, Long productId);

    @Transactional
    void deleteByUserId(Long userId);
}


