package com.app.musicstore.repository;

import com.app.musicstore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerId(Long sellerId);

    long countBySellerId(Long sellerId);

    List<Product> findBySellerIdAndStockGreaterThan(Long sellerId, int stock);

    List<Product> findByInstrumentType(String instrumentType);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByPriceBetween(double minPrice, double maxPrice);

    List<Product> findBySellerIdOrderByIdDesc(Long sellerId);

    List<Product> findBySellerIdAndNameContainingIgnoreCase(Long sellerId, String name);
}