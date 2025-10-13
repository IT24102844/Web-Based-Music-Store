package com.app.musicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "instrument_images")
public class InstrumentImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank(message = "Image URL is required")
    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    @Column(nullable = false)
    private String imageUrl;

    @Size(max = 255, message = "Image name cannot exceed 255 characters")
    private String imageName;
    
    @Pattern(regexp = "^(image/(jpeg|jpg|png|gif|webp))$", message = "Invalid image type")
    private String imageType;
    
    @Min(value = 1, message = "Image size must be greater than 0")
    @Max(value = 10485760, message = "Image size cannot exceed 10MB")
    private Long imageSize;

    @Column(nullable = false)
    private boolean isPrimary = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public InstrumentImg() {
        this.createdAt = LocalDateTime.now();
    }

    public InstrumentImg(Product product, String imageUrl, String imageName, String imageType, Long imageSize, boolean isPrimary) {
        this();
        this.product = product;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.imageType = imageType;
        this.imageSize = imageSize;
        this.isPrimary = isPrimary;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }

    public Long getImageSize() { return imageSize; }
    public void setImageSize(Long imageSize) { this.imageSize = imageSize; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}