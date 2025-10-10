package com.app.musicstore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "InstrumentSellers")
@PrimaryKeyJoinColumn(name = "userId")
public class InstrumentSeller extends User {

    private String paymentMethod; // e.g., "paypal,stripe"
    private String StoreName;
    private String StoreLocation;
    private String storeAddress;
    private String profileImagePath;


    public InstrumentSeller() {
        super();
    }

    public InstrumentSeller(String name, String email, String password, String paymentMethod, String StoreName, String StoreLocation) {
        super(name, email, password, Role.ITEM_SELLER);
        this.paymentMethod = paymentMethod;
        this.StoreName = StoreName;
        this.StoreLocation = StoreLocation;
    }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStoreName() { return StoreName; }
    public void setStoreName(String StoreName) { this.StoreName = StoreName; }
    public String getStoreLocation() { return StoreLocation; }
    public void setStoreLocation(String StoreLocation) { this.StoreLocation = StoreLocation; }
    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }
    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }
}
