package com.app.musicstore.service;

import com.app.musicstore.model.Order;
import com.app.musicstore.model.OrderItem;
import com.app.musicstore.model.Product;
import com.app.musicstore.repository.OrderItemRepository;
import com.app.musicstore.repository.OrderRepository;
import com.app.musicstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Order> getOrdersBySellerId(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    public List<Order> getOrdersByStatusAndSellerId(String status, Long sellerId) {
        return orderRepository.findByStatusAndSellerId(status, sellerId);
    }

    public long countOrdersByStatusAndSellerId(String status, Long sellerId) {
        Long count = orderRepository.countByStatusAndSellerId(status, sellerId);
        return count != null ? count : 0;
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(Long sellerId, Long customerId, String customerName, double total) {
        Order order = new Order();
        order.setSellerId(sellerId);
        order.setCustomerId(customerId);
        order.setCustomerName(customerName);
        order.setTotal(total);
        order.setStatus("PENDING");
        order.setDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order createOrderWithItems(Long sellerId,
                                      Long customerId,
                                      String customerName,
                                      List<Product> products,
                                      java.util.Map<Long, Integer> quantityByProductId) {
        Order order = new Order();
        order.setSellerId(sellerId);
        order.setCustomerId(customerId);
        order.setCustomerName(customerName);
        order.setStatus("PENDING");
        order.setDate(LocalDateTime.now());

        double total = 0.0;
        for (Product incomingProduct : products) {
            Long productId = incomingProduct.getId();
            int requestedQuantity = Math.max(1, quantityByProductId.getOrDefault(productId, 1));

            // Fetch latest product state to ensure correct stock in a transaction
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            // Validate stock
            if (product.getStock() < requestedQuantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            // Decrement stock
            product.setStock(product.getStock() - requestedQuantity);
            productRepository.save(product);

            double unitPrice = product.getPrice();
            total += unitPrice * requestedQuantity;

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImageUrl(product.getPrimaryImageUrl());
            item.setQuantity(requestedQuantity);
            item.setUnitPrice(unitPrice);
            order.addItem(item);
        }

        order.setTotal(total);
        Order saved = orderRepository.save(order);
        // Items cascade via order.addItem, but ensure persisted
        if (saved.getItems() != null && !saved.getItems().isEmpty()) {
            orderItemRepository.saveAll(saved.getItems());
        }
        return saved;
    }

    public Order updateOrderStatus(Long orderId, String newStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(newStatus);
            return orderRepository.save(order);
        }
        return null;
    }

    public Order shipOrder(Long orderId) {
        return updateOrderStatus(orderId, "SHIPPED");
    }

    public Order refundOrder(Long orderId) {
        return updateOrderStatus(orderId, "REFUNDED");
    }

    public Order completeOrder(Long orderId) {
        return updateOrderStatus(orderId, "COMPLETED");
    }

    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public double getTotalSalesBySellerId(Long sellerId) {
        List<Order> completedOrders = orderRepository.findByStatusAndSellerId("COMPLETED", sellerId);
        return completedOrders.stream()
                .mapToDouble(Order::getTotal)
                .sum();
    }

    public double getTotalSalesBySellerIdAndDateRange(Long sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> completedOrders = orderRepository.findByStatusAndSellerId("COMPLETED", sellerId);
        return completedOrders.stream()
                .filter(order -> order.getDate().isAfter(startDate) && order.getDate().isBefore(endDate))
                .mapToDouble(Order::getTotal)
                .sum();
    }
}
