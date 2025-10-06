package com.app.musicstore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "InstrumentSeller")
@PrimaryKeyJoinColumn(name = "sellerId")
public class InstrumentSeller extends User {

    private String storeName;

    private String location;

    // Constructors
    public InstrumentSeller() {}

    public InstrumentSeller(String storeName, String location) {
        this.storeName = storeName;
        this.location = location;
    }

    // Getters and Setters
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}