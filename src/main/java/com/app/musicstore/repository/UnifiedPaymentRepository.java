package com.app.musicstore.repository;

import com.app.musicstore.model.UnifiedPayment;
import com.app.musicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnifiedPaymentRepository extends JpaRepository<UnifiedPayment, Long> {
    List<UnifiedPayment> findByUserOrderByCreatedAtDesc(User user);

    Optional<UnifiedPayment> findByTransactionId(String transactionId);
}

