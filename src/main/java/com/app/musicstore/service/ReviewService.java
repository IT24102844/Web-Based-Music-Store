package com.app.musicstore.service;

import com.app.musicstore.model.Review;
import com.app.musicstore.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getReviewsBySellerId(Long sellerId) {
        return reviewRepository.findBySellerId(sellerId);
    }

    public List<Review> getApprovedReviewsBySellerId(Long sellerId) {
        return reviewRepository.findApprovedBySellerId(sellerId);
    }

    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public List<Review> getApprovedReviewsByProductId(Long productId) {
        return reviewRepository.findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId);
    }

    public Review createReview(Review review) {
        // Validate rating
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Upsert: update existing or create new
        Review existing = reviewRepository.findFirstByProductIdAndCustomerId(review.getProductId(), review.getCustomerId());
        if (existing != null) {
            existing.setRating(review.getRating());
            existing.setComment(review.getComment());
            existing.setUpdatedAt(LocalDateTime.now());
            return reviewRepository.save(existing);
        } else {
            review.setCreatedAt(LocalDateTime.now());
            review.setIsApproved(true); // Auto-approve for now
            return reviewRepository.save(review);
        }
    }

    public Review updateReview(Long reviewId, Review updatedReview) {
        Optional<Review> existingReview = reviewRepository.findById(reviewId);
        if (existingReview.isPresent()) {
            Review review = existingReview.get();
            review.setRating(updatedReview.getRating());
            review.setComment(updatedReview.getComment());
            review.setUpdatedAt(LocalDateTime.now());
            return reviewRepository.save(review);
        }
        return null;
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    public void approveReview(Long reviewId) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isPresent()) {
            review.get().setIsApproved(true);
            review.get().setUpdatedAt(LocalDateTime.now());
            reviewRepository.save(review.get());
        }
    }

    public void rejectReview(Long reviewId) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isPresent()) {
            review.get().setIsApproved(false);
            review.get().setUpdatedAt(LocalDateTime.now());
            reviewRepository.save(review.get());
        }
    }

    public double getAverageRatingBySellerId(Long sellerId) {
        Double average = reviewRepository.getAverageRatingBySellerId(sellerId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    public double getAverageRatingByProductId(Long productId) {
        Double average = reviewRepository.getAverageRatingByProductId(productId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    public Map<Long, Double> getAverageRatingsForProducts(java.util.Collection<Long> productIds) {
        Map<Long, Double> result = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return result;
        for (Object[] row : reviewRepository.findAverageRatingByProductIds(productIds)) {
            Long pid = (Long) row[0];
            Double avg = (Double) row[1];
            result.put(pid, avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        }
        return result;
    }

    public long getReviewCountBySellerId(Long sellerId) {
        Long count = reviewRepository.countBySellerId(sellerId);
        return count != null ? count : 0;
    }

    public long getReviewCountByProductId(Long productId) {
        Long count = reviewRepository.countByProductIdAndIsApprovedTrue(productId);
        return count != null ? count : 0;
    }

    public Map<Long, Long> getReviewCountsForProducts(java.util.Collection<Long> productIds) {
        Map<Long, Long> result = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return result;
        for (Object[] row : reviewRepository.findReviewCountByProductIds(productIds)) {
            Long pid = (Long) row[0];
            Long cnt = (Long) row[1];
            result.put(pid, cnt != null ? cnt : 0L);
        }
        return result;
    }

    public List<Review> getRecentReviewsBySellerId(Long sellerId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return reviewRepository.findRecentBySellerId(sellerId, since);
    }

    public List<Review> getReviewsByRatingRange(Long sellerId, int minRating, int maxRating) {
        return reviewRepository.findByProductIdAndRatingBetweenAndIsApprovedTrueOrderByCreatedAtDesc(sellerId, minRating, maxRating);
    }

    public List<Review> getReviewsByCustomerId(Long customerId) {
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public boolean hasCustomerReviewedProduct(Long productId, Long customerId) {
        return reviewRepository.existsByProductIdAndCustomerId(productId, customerId);
    }

    public Review getByProductAndCustomer(Long productId, Long customerId) {
        return reviewRepository.findFirstByProductIdAndCustomerId(productId, customerId);
    }

    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    // Get review statistics for dashboard
    public ReviewStats getReviewStatsBySellerId(Long sellerId) {
        System.out.println("=== REVIEW STATS DEBUG ===");
        System.out.println("Getting review stats for seller ID: " + sellerId);
        
        double averageRating = getAverageRatingBySellerId(sellerId);
        long totalReviews = getReviewCountBySellerId(sellerId);
        long recentReviews = getRecentReviewsBySellerId(sellerId, 30).size();
        
        System.out.println("Average Rating: " + averageRating);
        System.out.println("Total Reviews: " + totalReviews);
        System.out.println("Recent Reviews: " + recentReviews);
        
        // Calculate rating distribution
        long fiveStar = reviewRepository.findBySellerIdAndRatingBetweenAndIsApprovedTrueOrderByCreatedAtDesc(sellerId, 5, 5).size();
        long fourStar = reviewRepository.findBySellerIdAndRatingBetweenAndIsApprovedTrueOrderByCreatedAtDesc(sellerId, 4, 4).size();
        long threeStar = reviewRepository.findBySellerIdAndRatingBetweenAndIsApprovedTrueOrderByCreatedAtDesc(sellerId, 3, 3).size();
        long twoStar = reviewRepository.findBySellerIdAndRatingBetweenAndIsApprovedTrueOrderByCreatedAtDesc(sellerId, 2, 2).size();
        long oneStar = reviewRepository.findBySellerIdAndRatingBetweenAndIsApprovedTrueOrderByCreatedAtDesc(sellerId, 1, 1).size();

        System.out.println("Rating Distribution - 5★: " + fiveStar + ", 4★: " + fourStar + ", 3★: " + threeStar + ", 2★: " + twoStar + ", 1★: " + oneStar);
        System.out.println("=== END REVIEW STATS DEBUG ===");

        return new ReviewStats(averageRating, totalReviews, recentReviews, fiveStar, fourStar, threeStar, twoStar, oneStar);
    }

    // Inner class for review statistics
    public static class ReviewStats {
        private final double averageRating;
        private final long totalReviews;
        private final long recentReviews;
        private final long fiveStar;
        private final long fourStar;
        private final long threeStar;
        private final long twoStar;
        private final long oneStar;
        private final Map<Integer, Double> ratingDistribution;
        private final Map<Integer, Long> ratingCounts;

        public ReviewStats(double averageRating, long totalReviews, long recentReviews, 
                          long fiveStar, long fourStar, long threeStar, long twoStar, long oneStar) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
            this.recentReviews = recentReviews;
            this.fiveStar = fiveStar;
            this.fourStar = fourStar;
            this.threeStar = threeStar;
            this.twoStar = twoStar;
            this.oneStar = oneStar;
            
            // Calculate rating distribution percentages
            Map<Integer, Long> counts = new HashMap<>();
            counts.put(5, fiveStar);
            counts.put(4, fourStar);
            counts.put(3, threeStar);
            counts.put(2, twoStar);
            counts.put(1, oneStar);
            this.ratingCounts = counts;
            
            Map<Integer, Double> distribution = new HashMap<>();
            distribution.put(5, totalReviews > 0 ? (double) fiveStar / totalReviews * 100 : 0.0);
            distribution.put(4, totalReviews > 0 ? (double) fourStar / totalReviews * 100 : 0.0);
            distribution.put(3, totalReviews > 0 ? (double) threeStar / totalReviews * 100 : 0.0);
            distribution.put(2, totalReviews > 0 ? (double) twoStar / totalReviews * 100 : 0.0);
            distribution.put(1, totalReviews > 0 ? (double) oneStar / totalReviews * 100 : 0.0);
            this.ratingDistribution = distribution;
        }

        // Getters
        public double getAverageRating() { return averageRating; }
        public long getTotalReviews() { return totalReviews; }
        public long getRecentReviews() { return recentReviews; }
        public long getFiveStar() { return fiveStar; }
        public long getFourStar() { return fourStar; }
        public long getThreeStar() { return threeStar; }
        public long getTwoStar() { return twoStar; }
        public long getOneStar() { return oneStar; }
        public Map<Integer, Double> getRatingDistribution() { return ratingDistribution; }
        public Map<Integer, Long> getRatingCounts() { return ratingCounts; }
    }
}
