package com.app.musicstore.strategy.payment;

import com.app.musicstore.model.UnifiedPayment;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Concrete Strategy: Digital Wallet Payment
 * 
 * Handles payment processing via digital wallets (e.g., PayPal, Google Pay,
 * Apple Pay)
 */
@Component("WALLET")
public class WalletPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(UnifiedPayment payment) {
        System.out.println("📱 Processing Wallet Payment...");

        if (!validatePayment(payment)) {
            return PaymentResult.failure("Invalid payment details", "INVALID_DATA");
        }

        try {
            // Simulate wallet payment processing
            String transactionId = "WLT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Simulate instant processing (wallets are faster)
            Thread.sleep(200);

            // Simulate 99% success rate (wallets have highest success rate)
            if (Math.random() < 0.99) {
                System.out.println("✅ Wallet Payment Successful: " + transactionId);
                return PaymentResult.success(
                        transactionId,
                        "Payment processed successfully via Digital Wallet");
            } else {
                System.out.println("❌ Wallet Payment Failed: Insufficient balance");
                return PaymentResult.failure("Insufficient wallet balance", "INSUFFICIENT_BALANCE");
            }

        } catch (Exception e) {
            System.err.println("❌ Wallet Processing Error: " + e.getMessage());
            return PaymentResult.failure("Payment processing error: " + e.getMessage(), "PROCESSING_ERROR");
        }
    }

    @Override
    public boolean validatePayment(UnifiedPayment payment) {
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            System.out.println("❌ Invalid amount");
            return false;
        }

        // Wallets have high limits (e.g., 1,000,000 LKR)
        if (payment.getAmount() > 1000000) {
            System.out.println("❌ Amount exceeds wallet limit");
            return false;
        }

        System.out.println("✅ Wallet payment validated");
        return true;
    }

    @Override
    public String getPaymentMethod() {
        return "WALLET";
    }

    @Override
    public boolean supportsRefund() {
        return true;
    }
}
