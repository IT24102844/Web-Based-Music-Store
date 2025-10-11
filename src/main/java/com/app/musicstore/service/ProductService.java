package com.app.musicstore.service;

import com.app.musicstore.model.InstrumentImg;
import com.app.musicstore.model.Product;
import com.app.musicstore.repository.InstrumentImgRepository;
import com.app.musicstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InstrumentImgRepository instrumentImgRepository;

    private static final String UPLOAD_DIR = "uploads/Items/";

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    public List<Product> getProductsBySellerId(Long sellerId) {
        System.out.println("ProductService.getProductsBySellerId called with sellerId: " + sellerId);
        List<Product> products = productRepository.findBySellerId(sellerId);
        System.out.println("Repository returned " + (products != null ? products.size() : 0) + " products");
        return products;
    }

    public List<Product> getAvailableProductsBySellerId(Long sellerId) {
        return productRepository.findBySellerIdAndStockGreaterThan(sellerId, 0);
    }

    public Optional<Product> getProductById(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            // Force loading of images to ensure we have the latest state
            product.getImages().size(); // This triggers lazy loading
            return Optional.of(product);
        }
        return productOpt;
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setSpecifications(updatedProduct.getSpecifications());
            product.setPrice(updatedProduct.getPrice());
            product.setStock(updatedProduct.getStock());
            product.setInstrumentType(updatedProduct.getInstrumentType());
            return productRepository.save(product);
        }
        return null;
    }

    public void deleteProduct(Long id) {
        // Delete associated images first
        instrumentImgRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    public Product addProduct(Product product, List<MultipartFile> imageFiles) throws IOException {
        System.out.println("ProductService.addProduct called with product: " + product.getName());
        System.out.println("Image files: " + (imageFiles != null ? imageFiles.size() : 0));
        
        Product savedProduct = productRepository.save(product);
        System.out.println("Product saved with ID: " + savedProduct.getId());
        
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<InstrumentImg> images = new ArrayList<>();
            boolean isFirstImage = true;
            
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    System.out.println("Processing image file: " + file.getOriginalFilename());
                    String fileName = saveImageFile(file);
                    System.out.println("Image saved as: " + fileName);
                    
                    InstrumentImg image = new InstrumentImg();
                    image.setProduct(savedProduct);
                    image.setImageUrl("/uploads/Items/" + fileName);
                    image.setImageName(file.getOriginalFilename());
                    image.setImageType(file.getContentType());
                    image.setImageSize(file.getSize());
                    image.setPrimary(isFirstImage); // First image is primary
                    image.setCreatedAt(LocalDateTime.now());
                    
                    images.add(image);
                    isFirstImage = false;
                }
            }
            
            if (!images.isEmpty()) {
                instrumentImgRepository.saveAll(images);
                System.out.println("Saved " + images.size() + " images");
                savedProduct.setImages(images);
            }
        }
        
        return savedProduct;
    }

    public Product updateProductWithImages(Long id, Product updatedProduct, List<MultipartFile> newImageFiles) throws IOException {
        System.out.println("updateProductWithImages called for product ID: " + id);
        System.out.println("Updated product name: " + updatedProduct.getName());
        System.out.println("New image files: " + (newImageFiles != null ? newImageFiles.size() : 0));
        
        Product product = updateProduct(id, updatedProduct);
        System.out.println("Product updated, ID: " + (product != null ? product.getId() : "null"));
        
        if (product != null && newImageFiles != null && !newImageFiles.isEmpty()) {
            List<InstrumentImg> newImages = new ArrayList<>();
            
            for (MultipartFile file : newImageFiles) {
                if (!file.isEmpty()) {
                    System.out.println("Processing new image: " + file.getOriginalFilename());
                    String fileName = saveImageFile(file);
                    InstrumentImg image = new InstrumentImg();
                    image.setProduct(product);
                    image.setImageUrl("/uploads/Items/" + fileName);
                    image.setImageName(file.getOriginalFilename());
                    image.setImageType(file.getContentType());
                    image.setImageSize(file.getSize());
                    image.setPrimary(false); // New images are not primary by default
                    image.setCreatedAt(LocalDateTime.now());
                    
                    newImages.add(image);
                }
            }
            
            if (!newImages.isEmpty()) {
                instrumentImgRepository.saveAll(newImages);
                System.out.println("Saved " + newImages.size() + " new images");
            }
        }
        
        return product;
    }

    public void setPrimaryImage(Long productId, Long imageId) {
        // Clear all primary flags for this product
        instrumentImgRepository.clearPrimaryFlagsByProductId(productId);
        // Set the selected image as primary
        instrumentImgRepository.setPrimaryById(imageId);
    }

    public void deleteImage(Long imageId) {
        // Get the image first to find the product
        Optional<InstrumentImg> imageOpt = instrumentImgRepository.findById(imageId);
        if (imageOpt.isPresent()) {
            InstrumentImg image = imageOpt.get();
            Product product = image.getProduct();
            
            // Delete the image from database
            instrumentImgRepository.deleteById(imageId);
            
            // Remove the image from the product's images collection
            if (product != null && product.getImages() != null) {
                product.getImages().removeIf(img -> img.getId().equals(imageId));
            }
        } else {
            // If image not found, just delete by ID (fallback)
            instrumentImgRepository.deleteById(imageId);
        }
    }

    private String saveImageFile(MultipartFile file) throws IOException {
        System.out.println("saveImageFile called with file: " + file.getOriginalFilename());
        System.out.println("Upload directory: " + UPLOAD_DIR);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            System.out.println("Creating upload directory: " + uploadPath.toAbsolutePath());
            Files.createDirectories(uploadPath);
        } else {
            System.out.println("Upload directory exists: " + uploadPath.toAbsolutePath());
        }

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            originalFileName = "image";
        }
        String fileExtension = originalFileName.contains(".") ? 
            originalFileName.substring(originalFileName.lastIndexOf(".")) : ".jpg";
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        System.out.println("Generated filename: " + fileName);

        // Save file
        Path filePath = uploadPath.resolve(fileName);
        System.out.println("Saving file to: " + filePath.toAbsolutePath());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File saved successfully");

        return fileName;
    }

    public long countProductsBySellerId(Long sellerId) {
        return productRepository.countBySellerId(sellerId);
    }

    public List<Product> searchProductsBySellerId(Long sellerId, String searchTerm) {
        return productRepository.findBySellerId(sellerId).stream()
                .filter(product -> product.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();
    }

    public List<Product> getProductsByInstrumentType(String instrumentType) {
        return productRepository.findByInstrumentType(instrumentType);
    }

}
