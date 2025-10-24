package com.app.musicstore.strategy.payment;

import com.app.musicstore.model.UnifiedPayment;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Concrete Strategy: Cash on Delivery Payment
 * 
 * Handles payment processing for cash on delivery orders (instruments only)
 */
@Component("CASH_ON_DELIVERY")
public class CashOnDeliveryStrategy implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(UnifiedPayment payment) {
        System.out.println("💵 Processing Cash on Delivery...");

        if (!validatePayment(payment)) {
            return PaymentResult.failure("Invalid payment details", "INVALID_DATA");
        }

        try {
            // COD doesn't process payment immediately, just creates pending order
            String transactionId = "COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            System.out.println("📦 Cash on Delivery Order Created: " + transactionId);
            return PaymentResult.pending(
                    transactionId,
                    "Order placed. Payment will be collected on delivery.");

        } catch (Exception e) {
            System.err.println("❌ COD Processing Error: " + e.getMessage());
            return PaymentResult.failure("Order processing error: " + e.getMessage(), "PROCESSING_ERROR");
        }
    }

    @Override
    public boolean validatePayment(UnifiedPayment payment) {
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            System.out.println("❌ Invalid amount");
            return false;
        }

        // COD only for physical items (INSTRUMENT), not digital items (SONG, COURSE)
        if (!payment.getItemType().equals("INSTRUMENT")) {
            System.out.println("❌ COD not available for digital items");
            return false;
        }

        // COD has lower limit (e.g., 50,000 LKR)
        if (payment.getAmount() > 50000) {
            System.out.println("❌ Amount exceeds COD limit");
            return false;
        }

        System.out.println("✅ Cash on Delivery validated");
        return true;
    }

    @Override
    public String getPaymentMethod() {
        return "CASH_ON_DELIVERY";
    }

    @Override
    public boolean supportsRefund() {
        return false; // COD doesn't support refunds until delivery is completed
    }
}
