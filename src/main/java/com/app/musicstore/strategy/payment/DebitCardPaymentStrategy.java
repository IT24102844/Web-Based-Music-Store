package com.app.musicstore.strategy.payment;

import com.app.musicstore.model.UnifiedPayment;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Concrete Strategy: Debit Card Payment
 * 
 * Handles payment processing via debit card
 */
@Component("DEBIT_CARD")
public class DebitCardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(UnifiedPayment payment) {
        System.out.println("💳 Processing Debit Card Payment...");

        if (!validatePayment(payment)) {
            return PaymentResult.failure("Invalid payment details", "INVALID_DATA");
        }

        try {
            // Simulate debit card processing
            String transactionId = "DC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Simulate processing delay
            Thread.sleep(400);

            // Simulate 98% success rate (debit cards have higher success rate)
            if (Math.random() < 0.98) {
                System.out.println("✅ Debit Card Payment Successful: " + transactionId);
                return PaymentResult.success(
                        transactionId,
                        "Payment processed successfully via Debit Card");
            } else {
                System.out.println("❌ Debit Card Payment Failed: Daily limit exceeded");
                return PaymentResult.failure("Daily transaction limit exceeded", "LIMIT_EXCEEDED");
            }

        } catch (Exception e) {
            System.err.println("❌ Debit Card Processing Error: " + e.getMessage());
            return PaymentResult.failure("Payment processing error: " + e.getMessage(), "PROCESSING_ERROR");
        }
    }

    @Override
    public boolean validatePayment(UnifiedPayment payment) {
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            System.out.println("❌ Invalid amount");
            return false;
        }

        // Debit cards typically have lower limits (e.g., 200,000 LKR)
        if (payment.getAmount() > 200000) {
            System.out.println("❌ Amount exceeds debit card limit");
            return false;
        }

        System.out.println("✅ Debit Card payment validated");
        return true;
    }

    @Override
    public String getPaymentMethod() {
        return "DEBIT_CARD";
    }

    @Override
    public boolean supportsRefund() {
        return true;
    }
}
