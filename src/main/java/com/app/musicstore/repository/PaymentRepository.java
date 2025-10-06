package com.app.musicstore.repository;

import com.app.musicstore.model.Payment;
import com.app.musicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser(User user);
    List<Payment> findByUserOrderByCreatedAtDesc(User user);
    Payment findByTransactionId(String transactionId);
}