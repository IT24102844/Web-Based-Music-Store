package com.app.musicstore.strategy.payment;

import com.app.musicstore.model.UnifiedPayment;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Concrete Strategy: Credit Card Payment
 * 
 * Handles payment processing via credit card
 */
@Component("CREDIT_CARD")
public class CreditCardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(UnifiedPayment payment) {
        System.out.println("💳 Processing Credit Card Payment...");

        // Validate payment first
        if (!validatePayment(payment)) {
            return PaymentResult.failure("Invalid payment details", "INVALID_DATA");
        }

        try {
            // Simulate credit card processing
            // In real implementation, you would integrate with payment gateway like Stripe,
            // PayPal, etc.

            String transactionId = "CC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Simulate processing delay
            Thread.sleep(500);

            // Simulate 95% success rate
            if (Math.random() < 0.95) {
                System.out.println("✅ Credit Card Payment Successful: " + transactionId);
                return PaymentResult.success(
                        transactionId,
                        "Payment processed successfully via Credit Card");
            } else {
                System.out.println("❌ Credit Card Payment Failed: Insufficient funds");
                return PaymentResult.failure("Insufficient funds", "INSUFFICIENT_FUNDS");
            }

        } catch (Exception e) {
            System.err.println("❌ Credit Card Processing Error: " + e.getMessage());
            return PaymentResult.failure("Payment processing error: " + e.getMessage(), "PROCESSING_ERROR");
        }
    }

    @Override
    public boolean validatePayment(UnifiedPayment payment) {
        // Validate payment amount
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            System.out.println("❌ Invalid amount");
            return false;
        }

        // Check maximum transaction limit for credit cards (e.g., 500,000 LKR)
        if (payment.getAmount() > 500000) {
            System.out.println("❌ Amount exceeds credit card limit");
            return false;
        }

        System.out.println("✅ Credit Card payment validated");
        return true;
    }

    @Override
    public String getPaymentMethod() {
        return "CREDIT_CARD";
    }

    @Override
    public boolean supportsRefund() {
        return true; // Credit cards support refunds
    }
}
