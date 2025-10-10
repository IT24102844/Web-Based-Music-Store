package com.app.musicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 2000, message = "Specifications cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String specifications;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999,999.99")
    @Column(nullable = false)
    private double price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Max(value = 9999, message = "Stock quantity cannot exceed 9999")
    @Column(nullable = false)
    private int stock;

    @Column(name = "instrument_type")
    private String instrumentType;

    // One-to-Many relationship with InstrumentImg
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InstrumentImg> images = new ArrayList<>();

    // Constructors
    public Product() {}

    public Product(Long sellerId, String name, String description, double price, int stock, String instrumentType) {
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.instrumentType = instrumentType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getInstrumentType() { return instrumentType; }
    public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }

    public List<InstrumentImg> getImages() { return images; }
    public void setImages(List<InstrumentImg> images) {
        this.images = images;
        if (images != null) {
            for (InstrumentImg image : images) {
                image.setProduct(this);
            }
        }
    }

    // Helper method to get primary image URL
    public String getPrimaryImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.stream()
                    .filter(InstrumentImg::isPrimary)
                    .findFirst()
                    .map(InstrumentImg::getImageUrl)
                    .orElse(images.get(0).getImageUrl()); // Return first image if no primary found
        }
        return null;
    }

    // Helper method to add image
    public void addImage(InstrumentImg image) {
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(image);
        image.setProduct(this);
    }

    // Helper method to remove image
    public void removeImage(InstrumentImg image) {
        if (images != null) {
            images.remove(image);
            image.setProduct(null);
        }
    }

    // Helper method to check if product is in stock
    public boolean isInStock() {
        return stock > 0;
    }

    // Helper method to check if product is available
    public boolean isAvailable() {
        return stock > 0;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sellerId=" + sellerId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", instrumentType='" + instrumentType + '\'' +
                '}';
    }
}