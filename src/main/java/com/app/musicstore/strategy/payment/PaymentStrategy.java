package com.app.musicstore.strategy.payment;

import com.app.musicstore.model.UnifiedPayment;

/**
 * Strategy Pattern: Payment Strategy Interface
 * 
 * This interface defines the contract for different payment methods.
 * Each concrete implementation handles payment processing differently.
 */
public interface PaymentStrategy {

    /**
     * Process payment using the specific payment method
     * 
     * @param payment The payment object containing payment details
     * @return PaymentResult containing success status and transaction details
     */
    PaymentResult processPayment(UnifiedPayment payment);

    /**
     * Validate payment details before processing
     * 
     * @param payment The payment to validate
     * @return true if payment details are valid, false otherwise
     */
    boolean validatePayment(UnifiedPayment payment);

    /**
     * Get the payment method name
     * 
     * @return String representing the payment method (e.g., "CREDIT_CARD",
     *         "WALLET")
     */
    String getPaymentMethod();

    /**
     * Check if refund is supported for this payment method
     * 
     * @return true if refunds are supported, false otherwise
     */
    boolean supportsRefund();
}
