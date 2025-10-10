package com.app.musicstore.repository;

import com.app.musicstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findBySellerId(Long sellerId);

    List<Order> findByCustomerId(Long customerId);

    // Get orders for a customer by status
    List<Order> findByCustomerIdAndStatus(Long customerId, String status);
    List<Order> findByCustomerIdAndStatusIn(Long customerId, java.util.Collection<String> statuses);

    // Count orders for a seller with specific status
    Long countByStatusAndSellerId(String status, Long sellerId);

    // Get all orders with specific status for a seller
    List<Order> findByStatusAndSellerId(String status, Long sellerId);

    // Get orders by seller and date range
    List<Order> findBySellerIdAndDateBetween(Long sellerId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
