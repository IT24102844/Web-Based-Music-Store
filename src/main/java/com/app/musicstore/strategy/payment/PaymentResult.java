package com.app.musicstore.strategy.payment;

import java.time.LocalDateTime;

/**
 * Result object returned by PaymentStrategy implementations
 */
public class PaymentResult {

    private boolean success;
    private String transactionId;
    private String message;
    private String status; // SUCCESS, FAILED, PENDING
    private LocalDateTime processedAt;
    private String errorCode;

    public PaymentResult() {
        this.processedAt = LocalDateTime.now();
    }

    public PaymentResult(boolean success, String transactionId, String message, String status) {
        this.success = success;
        this.transactionId = transactionId;
        this.message = message;
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }

    // Static factory methods for common scenarios
    public static PaymentResult success(String transactionId, String message) {
        return new PaymentResult(true, transactionId, message, "SUCCESS");
    }

    public static PaymentResult failure(String message, String errorCode) {
        PaymentResult result = new PaymentResult(false, null, message, "FAILED");
        result.setErrorCode(errorCode);
        return result;
    }

    public static PaymentResult pending(String transactionId, String message) {
        return new PaymentResult(false, transactionId, message, "PENDING");
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
