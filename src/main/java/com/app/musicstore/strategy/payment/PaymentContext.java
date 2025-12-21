package com.app.musicstore.strategy.payment;

import com.app.musicstore.model.UnifiedPayment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Context Class for Payment Strategy Pattern
 * 
 * This class maintains a reference to one of the PaymentStrategy objects
 * and delegates payment processing to the selected strategy.
 */
@Component
public class PaymentContext {

    private final Map<String, PaymentStrategy> paymentStrategies;
    private PaymentStrategy currentStrategy;

    // Spring will auto-inject all PaymentStrategy beans into this map
    // The map key will be the component name (e.g., "CREDIT_CARD", "WALLET")
    public PaymentContext(Map<String, PaymentStrategy> paymentStrategies) {
        this.paymentStrategies = paymentStrategies;
        System.out.println("💡 PaymentContext initialized with " + paymentStrategies.size() + " strategies");
        paymentStrategies.keySet().forEach(key -> System.out.println("   - " + key + " strategy available"));
    }

    /**
     * Set the payment strategy to use
     * 
     * @param paymentMethod The payment method (e.g., "CREDIT_CARD", "WALLET")
     * @throws IllegalArgumentException if payment method is not supported
     */
    public void setPaymentStrategy(String paymentMethod) {
        PaymentStrategy strategy = paymentStrategies.get(paymentMethod);

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Payment method '" + paymentMethod + "' is not supported. " +
                            "Available methods: " + paymentStrategies.keySet());
        }

        this.currentStrategy = strategy;
        System.out.println("✅ Payment strategy set to: " + paymentMethod);
    }

    /**
     * Execute payment using the currently selected strategy
     * 
     * @param payment The payment to process
     * @return PaymentResult containing the result of payment processing
     * @throws IllegalStateException if no strategy is set
     */
    public PaymentResult executePayment(UnifiedPayment payment) {
        if (currentStrategy == null) {
            throw new IllegalStateException("No payment strategy selected. Call setPaymentStrategy() first.");
        }

        System.out.println("🔄 Executing payment using: " + currentStrategy.getPaymentMethod());
        return currentStrategy.processPayment(payment);
    }

    /**
     * Validate payment using the currently selected strategy
     * 
     * @param payment The payment to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePayment(UnifiedPayment payment) {
        if (currentStrategy == null) {
            throw new IllegalStateException("No payment strategy selected.");
        }

        return currentStrategy.validatePayment(payment);
    }

    /**
     * Check if the current strategy supports refunds
     * 
     * @return true if refunds are supported
     */
    public boolean supportsRefund() {
        if (currentStrategy == null) {
            return false;
        }
        return currentStrategy.supportsRefund();
    }

    /**
     * Get all available payment methods
     * 
     * @return Set of payment method names
     */
    public java.util.Set<String> getAvailablePaymentMethods() {
        return paymentStrategies.keySet();
    }

    /**
     * Check if a payment method is available
     * 
     * @param paymentMethod The payment method to check
     * @return true if available, false otherwise
     */
    public boolean isPaymentMethodAvailable(String paymentMethod) {
        return paymentStrategies.containsKey(paymentMethod);
    }
}
