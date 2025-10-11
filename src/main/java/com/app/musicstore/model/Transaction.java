package com.app.musicstore.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId; // link to Seller/User
    private LocalDateTime date = LocalDateTime.now();
    private double amount;
    private String type;  // SALE, REFUND
    private String status; // PENDING, COMPLETED

    public Transaction() {}

    public Transaction(Long sellerId, LocalDateTime date, double amount, String type, String status) {
        this.sellerId = sellerId;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
