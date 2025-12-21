# 🎨 Strategy Pattern Implementation - TuneWave Music Store

## 📋 Overview

This document explains the **Strategy Pattern** implementation for the payment processing system in TuneWave Music Store.

---

## 🎯 What is Strategy Pattern?

The **Strategy Pattern** is a behavioral design pattern that:
- Defines a family of algorithms (payment methods)
- Encapsulates each algorithm (Credit Card, Debit Card, Wallet, COD)
- Makes them interchangeable at runtime
- Allows the algorithm to vary independently from clients that use it

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    PaymentController                         │
│  (Receives payment request with selected payment method)    │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│                 UnifiedPaymentService                        │
│  (Business logic layer - uses PaymentContext)               │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│                    PaymentContext                            │
│  (Context class - selects and executes strategy)            │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│                  PaymentStrategy (Interface)                 │
│  + processPayment()                                          │
│  + validatePayment()                                         │
│  + getPaymentMethod()                                        │
│  + supportsRefund()                                          │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┬──────────────┐
        │               │               │              │
        ↓               ↓               ↓              ↓
┌──────────────┐ ┌──────────────┐ ┌──────────┐ ┌──────────────┐
│ CreditCard   │ │  DebitCard   │ │  Wallet  │ │ CashOnDelivery│
│  Strategy    │ │   Strategy   │ │ Strategy │ │   Strategy    │
└──────────────┘ └──────────────┘ └──────────┘ └──────────────┘
```

---

## 📁 File Structure

```
src/main/java/com/app/musicstore/
├── strategy/
│   └── payment/
│       ├── PaymentStrategy.java              ← Interface
│       ├── PaymentResult.java                ← Result DTO
│       ├── PaymentContext.java               ← Context Class
│       ├── CreditCardPaymentStrategy.java    ← Concrete Strategy
│       ├── DebitCardPaymentStrategy.java     ← Concrete Strategy
│       ├── WalletPaymentStrategy.java        ← Concrete Strategy
│       └── CashOnDeliveryStrategy.java       ← Concrete Strategy
├── service/
│   └── UnifiedPaymentService.java            ← Uses PaymentContext
├── controller/
│   └── PaymentController.java                ← Receives payment method
└── model/
    └── UnifiedPayment.java                   ← Updated with paymentMethod field
```

---

## 🔍 Component Details

### 1. **PaymentStrategy Interface**

```java
public interface PaymentStrategy {
    PaymentResult processPayment(UnifiedPayment payment);
    boolean validatePayment(UnifiedPayment payment);
    String getPaymentMethod();
    boolean supportsRefund();
}
```

**Purpose**: Defines the contract that all payment strategies must implement.

---

### 2. **Concrete Strategies**

#### **CreditCardPaymentStrategy**
- **Transaction ID Format**: `CC-XXXXXXXX`
- **Success Rate**: 95%
- **Max Amount**: LKR 500,000
- **Refund Support**: ✅ Yes
- **Use Case**: General purchases, high-value items

#### **DebitCardPaymentStrategy**
- **Transaction ID Format**: `DC-XXXXXXXX`
- **Success Rate**: 98%
- **Max Amount**: LKR 200,000
- **Refund Support**: ✅ Yes
- **Use Case**: Everyday purchases

#### **WalletPaymentStrategy**
- **Transaction ID Format**: `WLT-XXXXXXXX`
- **Success Rate**: 99%
- **Max Amount**: LKR 1,000,000
- **Refund Support**: ✅ Yes
- **Processing Time**: Fastest (200ms simulation)
- **Use Case**: Quick payments, frequent buyers

#### **CashOnDeliveryStrategy**
- **Transaction ID Format**: `COD-XXXXXXXX`
- **Success Rate**: 100% (creates pending order)
- **Max Amount**: LKR 50,000
- **Refund Support**: ❌ No (until delivery)
- **Item Restriction**: Only INSTRUMENT (physical items)
- **Use Case**: Customers without digital payment methods

---

### 3. **PaymentContext**

```java
@Component
public class PaymentContext {
    private final Map<String, PaymentStrategy> paymentStrategies;
    
    public void setPaymentStrategy(String paymentMethod) { ... }
    public PaymentResult executePayment(UnifiedPayment payment) { ... }
    public Set<String> getAvailablePaymentMethods() { ... }
}
```

**Purpose**: 
- Manages all payment strategies
- Selects the appropriate strategy at runtime
- Executes payment using the selected strategy

**Spring Auto-wiring**: All `PaymentStrategy` beans are automatically injected into the `Map<String, PaymentStrategy>`

---

### 4. **PaymentResult**

```java
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private String message;
    private String status;
    private String errorCode;
    
    // Factory methods
    public static PaymentResult success(...) { ... }
    public static PaymentResult failure(...) { ... }
    public static PaymentResult pending(...) { ... }
}
```

**Purpose**: Encapsulates the result of payment processing

---

## 🔄 How It Works (Flow)

### **Step-by-Step Execution**

1. **User selects payment method** on checkout page
   ```
   Item: "Guitar Lessons" (COURSE)
   Amount: LKR 5000
   Method: WALLET
   ```

2. **PaymentController receives request**
   ```java
   @PostMapping("/pay")
   public String pay(..., @RequestParam String paymentMethod, ...) {
       // paymentMethod = "WALLET"
   }
   ```

3. **UnifiedPaymentService processes payment**
   ```java
   public UnifiedPayment processPayment(..., String paymentMethod) {
       // Create payment object
       UnifiedPayment payment = new UnifiedPayment();
       // ... set properties
       
       // Use Strategy Pattern
       paymentContext.setPaymentStrategy(paymentMethod); // "WALLET"
       PaymentResult result = paymentContext.executePayment(payment);
       
       payment.setStatus(result.getStatus());
       payment.setTransactionId(result.getTransactionId());
   }
   ```

4. **PaymentContext selects strategy**
   ```java
   public void setPaymentStrategy(String paymentMethod) {
       PaymentStrategy strategy = paymentStrategies.get("WALLET");
       this.currentStrategy = strategy; // WalletPaymentStrategy instance
   }
   ```

5. **Strategy executes payment**
   ```java
   // WalletPaymentStrategy.processPayment() is called
   public PaymentResult processPayment(UnifiedPayment payment) {
       // Validate
       if (!validatePayment(payment)) return failure(...);
       
       // Process (simulate wallet payment)
       String transactionId = "WLT-A7B3C9D2";
       
       // Return success
       return PaymentResult.success(transactionId, "Payment via Wallet successful");
   }
   ```

6. **Result returned to user**
   ```
   Status: SUCCESS
   Transaction ID: WLT-A7B3C9D2
   Message: Payment via Wallet successful
   ```

---

## 🎯 Benefits of Strategy Pattern

### **1. Open/Closed Principle**
✅ Open for extension (add new payment methods)
❌ Closed for modification (don't change existing code)

**Example**: Adding a new "Bank Transfer" strategy
```java
@Component("BANK_TRANSFER")
public class BankTransferStrategy implements PaymentStrategy {
    // Implementation
}
```
No need to modify existing strategies or PaymentContext!

---

### **2. Single Responsibility Principle**
Each strategy handles ONE payment method only:
- `CreditCardPaymentStrategy` → Credit card logic only
- `WalletPaymentStrategy` → Wallet logic only

---

### **3. Runtime Flexibility**
Payment method can be changed at runtime based on:
- User selection
- Item type (COD only for physical items)
- Amount (different strategies have different limits)
- Business rules

---

### **4. Easy Testing**
Each strategy can be tested independently:
```java
@Test
public void testCreditCardPayment() {
    CreditCardPaymentStrategy strategy = new CreditCardPaymentStrategy();
    UnifiedPayment payment = new UnifiedPayment();
    // ... test only credit card logic
}
```

---

### **5. Maintainability**
- Bug in wallet payment? → Fix only `WalletPaymentStrategy`
- New validation rule for COD? → Modify only `CashOnDeliveryStrategy`

---

## 🔧 How to Add a New Payment Method

### **Example: Adding "Bank Transfer" Payment**

#### **Step 1: Create Strategy Class**
```java
@Component("BANK_TRANSFER")
public class BankTransferStrategy implements PaymentStrategy {
    
    @Override
    public PaymentResult processPayment(UnifiedPayment payment) {
        // Bank transfer processing logic
        String transactionId = "BNK-" + UUID.randomUUID();
        return PaymentResult.success(transactionId, "Bank transfer initiated");
    }
    
    @Override
    public boolean validatePayment(UnifiedPayment payment) {
        // Validation logic
        return payment.getAmount() != null && payment.getAmount() > 0;
    }
    
    @Override
    public String getPaymentMethod() {
        return "BANK_TRANSFER";
    }
    
    @Override
    public boolean supportsRefund() {
        return true;
    }
}
```

#### **Step 2: Update Controller (Optional)**
```java
availablePaymentMethods.add("BANK_TRANSFER");
```

#### **Step 3: That's it!**
Spring will automatically:
- ✅ Detect the new `@Component("BANK_TRANSFER")`
- ✅ Register it in `PaymentContext`
- ✅ Make it available for use

---

## 💡 For Your Viva

### **Key Points to Mention**

1. **"We implemented Strategy Pattern for payment processing"**
   - Show `PaymentStrategy` interface
   - Show concrete strategies (Credit Card, Wallet, etc.)

2. **"It follows SOLID principles"**
   - Open/Closed: Can add new payment methods without modifying existing code
   - Single Responsibility: Each strategy handles one payment method
   - Dependency Inversion: Depends on abstraction (interface), not concrete classes

3. **"Spring Framework integration"**
   - All strategies are Spring beans (`@Component`)
   - Auto-injected into `PaymentContext` using `Map<String, PaymentStrategy>`
   - No manual registration needed

4. **"Real-world benefits"**
   - Easy to add new payment gateways (Stripe, PayPal, etc.)
   - Each payment method can have different validation rules
   - Different transaction limits per method
   - Runtime selection based on business rules

5. **"Testability"**
   - Each strategy can be unit tested independently
   - Mock different payment scenarios easily

---

## 📊 Comparison: Before vs After

### **Before (Without Strategy Pattern)**
```java
public UnifiedPayment processPayment(..., String paymentMethod) {
    if (paymentMethod.equals("CREDIT_CARD")) {
        // 50 lines of credit card logic
    } else if (paymentMethod.equals("DEBIT_CARD")) {
        // 50 lines of debit card logic
    } else if (paymentMethod.equals("WALLET")) {
        // 50 lines of wallet logic
    } else if (paymentMethod.equals("COD")) {
        // 50 lines of COD logic
    }
    // 200+ lines in one method! 😱
}
```
**Problems**: 
- ❌ Hard to maintain
- ❌ Violates Single Responsibility Principle
- ❌ Difficult to test
- ❌ Adding new methods requires modifying this method

---

### **After (With Strategy Pattern)**
```java
public UnifiedPayment processPayment(..., String paymentMethod) {
    paymentContext.setPaymentStrategy(paymentMethod);
    PaymentResult result = paymentContext.executePayment(payment);
    // Clean, simple, maintainable! ✅
}
```
**Benefits**:
- ✅ Clean and readable
- ✅ Each payment method in separate class
- ✅ Easy to add new methods
- ✅ Easy to test

---

## 🎓 Interview/Viva Questions & Answers

**Q1: What design pattern did you use for payment processing?**
> "We implemented the Strategy Pattern to handle different payment methods. It allows us to encapsulate different payment algorithms (Credit Card, Wallet, COD) and make them interchangeable at runtime."

**Q2: Why Strategy Pattern instead of if-else?**
> "Strategy Pattern follows SOLID principles. With if-else, adding a new payment method requires modifying the main method. With Strategy, we just create a new class. It's also easier to test and maintain."

**Q3: How does Spring help with Strategy Pattern?**
> "Spring's dependency injection automatically detects all PaymentStrategy beans and injects them into our PaymentContext. We use @Component annotation with the payment method name as the bean name."

**Q4: How would you add a new payment method?**
> "Simply create a new class implementing PaymentStrategy, annotate it with @Component, and Spring will automatically register it. No changes needed to existing code."

**Q5: What are the benefits?**
> "Open/Closed Principle - open for extension, closed for modification. Each strategy is independently testable. Runtime flexibility in selecting payment methods. Clear separation of concerns."

---

## 🚀 Testing the Implementation

### **Test 1: Credit Card Payment**
```bash
curl -X POST http://localhost:8080/payments/pay \
  -d "itemType=SONG" \
  -d "itemId=1" \
  -d "itemName=Summer Vibes" \
  -d "amount=500" \
  -d "paymentMethod=CREDIT_CARD"
```

### **Test 2: Wallet Payment**
```bash
curl -X POST http://localhost:8080/payments/pay \
  -d "itemType=COURSE" \
  -d "itemId=5" \
  -d "itemName=Guitar Basics" \
  -d "amount=5000" \
  -d "paymentMethod=WALLET"
```

### **Test 3: Cash on Delivery (Instruments only)**
```bash
curl -X POST http://localhost:8080/payments/pay \
  -d "itemType=INSTRUMENT" \
  -d "itemId=10" \
  -d "itemName=Electric Guitar" \
  -d "amount=25000" \
  -d "paymentMethod=CASH_ON_DELIVERY"
```

---

## 📈 Future Enhancements

1. **Real Payment Gateway Integration**
   - Integrate Stripe API in `CreditCardPaymentStrategy`
   - Integrate PayPal in `WalletPaymentStrategy`

2. **Additional Strategies**
   - `BankTransferStrategy`
   - `CryptocurrencyStrategy`
   - `InstallmentPaymentStrategy`

3. **Advanced Validation**
   - Credit score check for high-value transactions
   - Fraud detection
   - Geographic restrictions

4. **Retry Logic**
   - Automatic retry for failed payments
   - Fallback to alternative payment method

---

## ✅ Checklist for Viva

- [ ] Understand the flow: Controller → Service → Context → Strategy
- [ ] Know all 4 concrete strategies and their differences
- [ ] Explain why Strategy Pattern is better than if-else
- [ ] Show how to add a new payment method
- [ ] Mention SOLID principles (especially Open/Closed)
- [ ] Explain Spring's role in auto-wiring strategies
- [ ] Know the transaction ID formats for each method
- [ ] Understand validation rules (amount limits, item type restrictions)

---

**Good luck with your viva! 🎓**

