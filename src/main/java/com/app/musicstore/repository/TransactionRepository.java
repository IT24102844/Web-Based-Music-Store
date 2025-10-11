package com.app.musicstore.repository;

import com.app.musicstore.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySellerId(Long sellerId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.sellerId = :sellerId AND t.status = :status")
    Double sumBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") String status);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.sellerId = :sellerId AND t.type = :type")
    Double sumBySellerIdAndType(@Param("sellerId") Long sellerId, @Param("type") String type);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.status = :status")
    Double sumByTypeAndStatus(@Param("type") String type, @Param("status") String status);
}
