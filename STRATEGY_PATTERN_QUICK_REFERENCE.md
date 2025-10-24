# 🎯 Strategy Pattern - Quick Reference Card

## 📝 One-Line Explanation
**"Strategy Pattern lets you swap payment algorithms (Credit Card, Wallet, COD) at runtime without changing the main code."**

---

## 🏗️ Three Main Components

| Component | File | Purpose |
|-----------|------|---------|
| **Strategy Interface** | `PaymentStrategy.java` | Defines the contract |
| **Concrete Strategies** | `CreditCardPaymentStrategy.java`, etc. | Implements specific payment methods |
| **Context** | `PaymentContext.java` | Selects and executes strategies |

---

## 💳 Payment Methods Comparison

| Method | Transaction ID | Success Rate | Max Amount | Refund | Item Restriction |
|--------|---------------|--------------|------------|--------|------------------|
| **Credit Card** | CC-XXXXXXXX | 95% | 500,000 | ✅ Yes | None |
| **Debit Card** | DC-XXXXXXXX | 98% | 200,000 | ✅ Yes | None |
| **Wallet** | WLT-XXXXXXXX | 99% | 1,000,000 | ✅ Yes | None |
| **Cash on Delivery** | COD-XXXXXXXX | 100% | 50,000 | ❌ No | INSTRUMENT only |

---

## 🔄 Execution Flow (5 Steps)

```
1. User selects payment method (e.g., "WALLET")
   ↓
2. PaymentController receives request
   ↓
3. UnifiedPaymentService calls PaymentContext
   ↓
4. PaymentContext selects WalletPaymentStrategy
   ↓
5. Strategy processes payment and returns result
```

---

## ✅ SOLID Principles Applied

| Principle | How We Use It |
|-----------|---------------|
| **Single Responsibility** | Each strategy handles ONE payment method |
| **Open/Closed** | Add new payment methods without modifying existing code |
| **Liskov Substitution** | All strategies are interchangeable (same interface) |
| **Interface Segregation** | PaymentStrategy interface is focused and minimal |
| **Dependency Inversion** | Depends on abstraction (interface), not concrete classes |

---

## 🆕 How to Add New Payment Method (3 Steps)

```java
// Step 1: Create new class
@Component("NEW_METHOD")
public class NewPaymentStrategy implements PaymentStrategy {
    public PaymentResult processPayment(UnifiedPayment payment) {
        // Your logic here
    }
    // ... other methods
}

// Step 2: That's it! Spring auto-registers it
// Step 3: Use it: paymentMethod = "NEW_METHOD"
```

---

## 🎤 Viva Answer Templates

### **Q: What is Strategy Pattern?**
> "It's a behavioral design pattern that defines a family of algorithms, encapsulates each one, and makes them interchangeable. In our project, we use it for payment processing where users can choose between Credit Card, Debit Card, Wallet, or Cash on Delivery."

### **Q: Why not use if-else statements?**
> "If-else violates the Open/Closed Principle. Every time we add a new payment method, we'd have to modify the main method. With Strategy Pattern, we just create a new class without touching existing code. It's also more testable and maintainable."

### **Q: How does Spring help?**
> "Spring automatically injects all PaymentStrategy beans into our PaymentContext using a Map. We just annotate strategies with @Component and Spring handles the registration."

### **Q: Show me the code flow**
> "Sure! When a user pays: PaymentController receives the payment method → calls UnifiedPaymentService → which uses PaymentContext → PaymentContext selects the right strategy (e.g., WalletPaymentStrategy) → Strategy processes the payment → Result returned."

### **Q: What are the benefits?**
> "1) Follows SOLID principles, 2) Easy to add new payment methods, 3) Each payment method is independently testable, 4) Runtime flexibility, 5) Clear separation of concerns."

---

## 🔍 Code Snippets for Demo

### **Interface Definition**
```java
public interface PaymentStrategy {
    PaymentResult processPayment(UnifiedPayment payment);
    boolean validatePayment(UnifiedPayment payment);
    String getPaymentMethod();
    boolean supportsRefund();
}
```

### **Concrete Strategy Example**
```java
@Component("WALLET")
public class WalletPaymentStrategy implements PaymentStrategy {
    public PaymentResult processPayment(UnifiedPayment payment) {
        String txnId = "WLT-" + UUID.randomUUID();
        return PaymentResult.success(txnId, "Wallet payment successful");
    }
    // ... other methods
}
```

### **Using the Strategy**
```java
// In UnifiedPaymentService
paymentContext.setPaymentStrategy("WALLET"); // Select strategy
PaymentResult result = paymentContext.executePayment(payment); // Execute
```

---

## 📊 Before vs After Comparison

### **❌ Before (Without Strategy)**
```java
if (method.equals("CREDIT_CARD")) {
    // 50 lines
} else if (method.equals("WALLET")) {
    // 50 lines
} // ... 200+ lines total
```
**Problems**: Hard to maintain, violates SRP, difficult to test

### **✅ After (With Strategy)**
```java
paymentContext.setPaymentStrategy(method);
PaymentResult result = paymentContext.executePayment(payment);
```
**Benefits**: Clean, maintainable, follows SOLID

---

## 🎯 Key Differences Between Strategies

| Feature | Credit/Debit Card | Wallet | Cash on Delivery |
|---------|-------------------|--------|------------------|
| **Processing Speed** | Medium | Fast ⚡ | Instant (pending) |
| **Validation** | Amount limits | Balance check | Physical items only |
| **Status** | SUCCESS/FAILED | SUCCESS/FAILED | PENDING |
| **Refund** | Immediate | Immediate | After delivery |
| **Real-world Use** | Bank API | PayPal/GPay | Courier handoff |

---

## 🧪 Testing Commands

```bash
# Test Credit Card
curl -X POST localhost:8080/payments/pay \
  -d "paymentMethod=CREDIT_CARD" -d "amount=500"

# Test Wallet
curl -X POST localhost:8080/payments/pay \
  -d "paymentMethod=WALLET" -d "amount=5000"
```

---

## ⚠️ Common Mistakes to Avoid

1. **❌ Don't hardcode payment methods in if-else**
   - ✅ Use Strategy Pattern instead

2. **❌ Don't mix validation with processing logic**
   - ✅ Separate `validatePayment()` method

3. **❌ Don't forget to annotate with @Component**
   - ✅ Spring needs it for auto-detection

4. **❌ Don't return null on failure**
   - ✅ Return PaymentResult.failure() with error details

---

## 🎓 Final Checklist

- [ ] I can explain what Strategy Pattern is
- [ ] I can list all 4 payment strategies
- [ ] I can explain the code flow (5 steps)
- [ ] I can name at least 2 SOLID principles we follow
- [ ] I can demonstrate adding a new payment method
- [ ] I know the difference between strategies (transaction ID, limits)
- [ ] I can explain why Strategy is better than if-else
- [ ] I understand Spring's role in auto-wiring

---

## 🚀 Bonus Points

**Real-World Applications of Strategy Pattern:**
- Payment processing (our use case)
- Sorting algorithms (QuickSort, MergeSort, BubbleSort)
- Compression algorithms (ZIP, RAR, 7z)
- Route finding (Fastest, Shortest, Scenic)
- Tax calculation (different countries, different rules)

---

**Print this card and keep it handy for your viva! 📄**

