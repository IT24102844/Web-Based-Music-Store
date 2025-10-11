package com.app.musicstore.repository;

import com.app.musicstore.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find reviews by product ID
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Find reviews by seller (through products)
    @Query("SELECT r FROM Review r JOIN Product p ON r.productId = p.id WHERE p.sellerId = :sellerId ORDER BY r.createdAt DESC")
    List<Review> findBySellerId(@Param("sellerId") Long sellerId);

    // Find approved reviews by product ID
    List<Review> findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(Long productId);

    // Find approved reviews by seller
    @Query("SELECT r FROM Review r JOIN Product p ON r.productId = p.id WHERE p.sellerId = :sellerId AND r.isApproved = true ORDER BY r.createdAt DESC")
    List<Review> findApprovedBySellerId(@Param("sellerId") Long sellerId);

    // Calculate average rating for a product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.isApproved = true")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    // Calculate average rating for all products by a seller
    @Query("SELECT AVG(r.rating) FROM Review r JOIN Product p ON r.productId = p.id WHERE p.sellerId = :sellerId AND r.isApproved = true")
    Double getAverageRatingBySellerId(@Param("sellerId") Long sellerId);

    // Count reviews for a product
    Long countByProductIdAndIsApprovedTrue(Long productId);

    // Count reviews for a seller
    @Query("SELECT COUNT(r) FROM Review r JOIN Product p ON r.productId = p.id WHERE p.sellerId = :sellerId AND r.isApproved = true")
    Long countBySellerId(@Param("sellerId") Long sellerId);

    // Find reviews by rating range
    List<Review> findByProductIdAndRatingBetweenAndIsApprovedTrueOrderByCreatedAtDesc(Long productId, Integer minRating, Integer maxRating);

    // Find reviews by seller and rating range
    @Query("SELECT r FROM Review r JOIN Product p ON r.productId = p.id WHERE p.sellerId = :sellerId AND r.rating BETWEEN :minRating AND :maxRating AND r.isApproved = true ORDER BY r.createdAt DESC")
    List<Review> findBySellerIdAndRatingBetweenAndIsApprovedTrueOrderByCreatedAtDesc(@Param("sellerId") Long sellerId, @Param("minRating") Integer minRating, @Param("maxRating") Integer maxRating);

    // Find recent reviews for a seller (last 30 days)
    @Query("SELECT r FROM Review r JOIN Product p ON r.productId = p.id WHERE p.sellerId = :sellerId AND r.isApproved = true AND r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<Review> findRecentBySellerId(@Param("sellerId") Long sellerId, @Param("since") java.time.LocalDateTime since);

    // Find reviews by customer
    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Check if customer has already reviewed a product
    boolean existsByProductIdAndCustomerId(Long productId, Long customerId);

    // Get a customer's review for a product
    Review findFirstByProductIdAndCustomerId(Long productId, Long customerId);

    // Batch: average rating for many product IDs
    @Query("SELECT r.productId, AVG(r.rating) FROM Review r WHERE r.productId IN :productIds AND r.isApproved = true GROUP BY r.productId")
    List<Object[]> findAverageRatingByProductIds(@Param("productIds") java.util.Collection<Long> productIds);

    // Batch: review count for many product IDs
    @Query("SELECT r.productId, COUNT(r) FROM Review r WHERE r.productId IN :productIds AND r.isApproved = true GROUP BY r.productId")
    List<Object[]> findReviewCountByProductIds(@Param("productIds") java.util.Collection<Long> productIds);
}
