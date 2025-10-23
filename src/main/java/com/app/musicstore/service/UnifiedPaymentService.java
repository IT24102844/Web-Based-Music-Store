package com.app.musicstore.service;

import com.app.musicstore.model.UnifiedPayment;
import com.app.musicstore.model.User;
import com.app.musicstore.repository.UnifiedPaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UnifiedPaymentService {

    private final UnifiedPaymentRepository unifiedPaymentRepository;

    public UnifiedPaymentService(UnifiedPaymentRepository unifiedPaymentRepository) {
        this.unifiedPaymentRepository = unifiedPaymentRepository;
    }

    public UnifiedPayment processMockPayment(User user,
            String itemType,
            Long itemId,
            String itemName,
            Double amount) {
        UnifiedPayment payment = new UnifiedPayment();
        payment.setUser(user);
        payment.setItemType(itemType);
        payment.setItemId(itemId);
        payment.setItemName(itemName);
        payment.setAmount(amount);
        payment.setStatus("SUCCESS");
        payment.setTransactionId("UP-" + UUID.randomUUID());
        payment.setPaymentDate(LocalDateTime.now());
        return unifiedPaymentRepository.save(payment);
    }

    public List<UnifiedPayment> getUserPayments(User user) {
        return unifiedPaymentRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public UnifiedPayment getById(Long id) {
        return unifiedPaymentRepository.findById(id).orElse(null);
    }
}

