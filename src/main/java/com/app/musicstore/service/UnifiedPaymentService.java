package com.app.musicstore.service;

import com.app.musicstore.model.UnifiedPayment;
import com.app.musicstore.model.User;
import com.app.musicstore.repository.UnifiedPaymentRepository;
import com.app.musicstore.strategy.payment.PaymentContext;
import com.app.musicstore.strategy.payment.PaymentResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UnifiedPaymentService {

    private final UnifiedPaymentRepository unifiedPaymentRepository;
    private final PaymentContext paymentContext;

    public UnifiedPaymentService(UnifiedPaymentRepository unifiedPaymentRepository,
            PaymentContext paymentContext) {
        this.unifiedPaymentRepository = unifiedPaymentRepository;
        this.paymentContext = paymentContext;
    }

    /**
     * Process payment using Strategy Pattern
     * 
     * @param user          User making the payment
     * @param itemType      Type of item (SONG, EVENT, INSTRUMENT, COURSE)
     * @param itemId        ID of the item
     * @param itemName      Name of the item
     * @param amount        Payment amount
     * @param paymentMethod Payment method (CREDIT_CARD, DEBIT_CARD, WALLET,
     *                      CASH_ON_DELIVERY)
     * @return Processed UnifiedPayment object
     */
    public UnifiedPayment processPayment(User user,
            String itemType,
            Long itemId,
            String itemName,
            Double amount,
            String paymentMethod) {

        System.out.println("💰 Processing payment with Strategy Pattern");
        System.out.println("   User: " + user.getEmail());
        System.out.println("   Item: " + itemName + " (" + itemType + ")");
        System.out.println("   Amount: LKR " + amount);
        System.out.println("   Method: " + paymentMethod);

        // Create payment object
        UnifiedPayment payment = new UnifiedPayment();
        payment.setUser(user);
        payment.setItemType(itemType);
        payment.setItemId(itemId);
        payment.setItemName(itemName);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());

        // Use Strategy Pattern to process payment
        try {
            // Set the payment strategy based on payment method
            paymentContext.setPaymentStrategy(paymentMethod);

            // Execute payment using the selected strategy
            PaymentResult result = paymentContext.executePayment(payment);

            // Update payment status based on result
            payment.setStatus(result.getStatus());
            payment.setTransactionId(result.getTransactionId());

            System.out.println("✅ Payment processed: " + result.getMessage());

        } catch (IllegalArgumentException e) {
            // Invalid payment method
            System.err.println("❌ Invalid payment method: " + e.getMessage());
            payment.setStatus("FAILED");
            payment.setTransactionId("FAILED-" + UUID.randomUUID().toString().substring(0, 8));
        } catch (Exception e) {
            // Other errors
            System.err.println("❌ Payment processing error: " + e.getMessage());
            payment.setStatus("FAILED");
            payment.setTransactionId("ERROR-" + UUID.randomUUID().toString().substring(0, 8));
        }

        // Save and return payment
        return unifiedPaymentRepository.save(payment);
    }

    /**
     * Legacy method for backward compatibility (defaults to CREDIT_CARD)
     * 
     * @deprecated Use processPayment with paymentMethod parameter instead
     */
    @Deprecated
    public UnifiedPayment processMockPayment(User user,
            String itemType,
            Long itemId,
            String itemName,
            Double amount) {
        return processPayment(user, itemType, itemId, itemName, amount, "CREDIT_CARD");
    }

    public List<UnifiedPayment> getUserPayments(User user) {
        return unifiedPaymentRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public UnifiedPayment getById(Long id) {
        return unifiedPaymentRepository.findById(id).orElse(null);
    }

    /**
     * Check if a user has successfully purchased a specific song
     */
    public boolean hasUserPurchasedSong(User user, Long songId) {
        if (user == null || songId == null) {
            return false;
        }
        return unifiedPaymentRepository.existsByUserAndItemTypeAndItemIdAndStatus(
                user, "SONG", songId, "SUCCESS");
    }
}
