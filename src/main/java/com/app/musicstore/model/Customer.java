package com.app.musicstore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "customer")
@PrimaryKeyJoinColumn(name = "userId")
public class Customer extends User {

    private String preferences;

    public Customer() {}

    public Customer(String name, String email, String password, String preferences) {
        super(name, email, password, Role.CUSTOMER);
        this.preferences = preferences;
    }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
}
