package com.app.musicstore.controller;

import com.app.musicstore.model.*;
import com.app.musicstore.repository.UserRepository;
import com.app.musicstore.service.*;
import com.app.musicstore.util.ImageValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());
            return optionalUser.orElse(null);
        }
        return null;
    }

    private boolean isSeller(User user) {
        return user != null &&
                (user.getRole() == Role.ITEM_SELLER || user.getRole() == Role.COURSE_SELLER);
    }

    // ---------------- DASHBOARD ----------------
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        Long sellerId = user.getUserId();

        model.addAttribute("user", user);
        model.addAttribute("totalProducts", productService.countProductsBySellerId(sellerId));
        model.addAttribute("pendingOrders", orderService.countOrdersByStatusAndSellerId("PENDING", sellerId));
        model.addAttribute("shipmentsInTransit", orderService.countOrdersByStatusAndSellerId("SHIPPED", sellerId));

        double totalSales = transactionService.getCompletedEarningsBySellerId(sellerId);
        model.addAttribute("totalSales", String.format("$%.2f", totalSales));

        // Get review statistics
        ReviewService.ReviewStats reviewStats = reviewService.getReviewStatsBySellerId(sellerId);
        model.addAttribute("averageRating", String.format("%.1f / 5", reviewStats.getAverageRating()));
        model.addAttribute("totalReviews", reviewStats.getTotalReviews());

        return "seller_dashboard";
    }

    // ---------------- SETTINGS ----------------
    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        // Get seller-specific data if user is an InstrumentSeller
        if (user instanceof InstrumentSeller) {
            InstrumentSeller seller = (InstrumentSeller) user;
            model.addAttribute("seller", seller);
            model.addAttribute("profileImagePath", seller.getProfileImagePath());
        } else {
            model.addAttribute("seller", user);
        }

        model.addAttribute("user", user);
        return "seller_settings";
    }

    @PostMapping("/updateSettings")
    public String updateSettings(@RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNo,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String storeAddress,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) MultipartFile profileImage,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        try {
            // Create a new user object with updated fields to avoid JPA issues
            User updatedUser = new User();
            updatedUser.setUserId(user.getUserId());

            // Update basic profile information
            if (name != null && !name.trim().isEmpty()) {
                updatedUser.setName(name);
            } else {
                updatedUser.setName(user.getName());
            }

            if (email != null && !email.trim().isEmpty()) {
                updatedUser.setEmail(email);
            } else {
                updatedUser.setEmail(user.getEmail());
            }

            if (phoneNo != null && !phoneNo.trim().isEmpty()) {
                updatedUser.setPhoneNo(phoneNo);
            } else {
                updatedUser.setPhoneNo(user.getPhoneNo());
            }

            if (address != null && !address.trim().isEmpty()) {
                updatedUser.setAddress(address);
            } else {
                updatedUser.setAddress(user.getAddress());
            }

            // Update seller-specific fields if user is an InstrumentSeller
            if (user instanceof InstrumentSeller) {
                InstrumentSeller existingSeller = (InstrumentSeller) user;
                InstrumentSeller updatedSeller = new InstrumentSeller();

                // Copy existing seller fields
                updatedSeller.setUserId(existingSeller.getUserId());
                updatedSeller.setName(updatedUser.getName());
                updatedSeller.setEmail(updatedUser.getEmail());
                updatedSeller.setPhoneNo(updatedUser.getPhoneNo());
                updatedSeller.setAddress(updatedUser.getAddress());
                updatedSeller.setRole(existingSeller.getRole());
                updatedSeller.setStatus(existingSeller.getStatus());
                updatedSeller.setCreatedAt(existingSeller.getCreatedAt());

                // Update seller-specific fields
                if (storeName != null && !storeName.trim().isEmpty()) {
                    updatedSeller.setStoreName(storeName);
                } else {
                    updatedSeller.setStoreName(existingSeller.getStoreName());
                }

                if (location != null && !location.trim().isEmpty()) {
                    updatedSeller.setStoreLocation(location);
                } else {
                    updatedSeller.setStoreLocation(existingSeller.getStoreLocation());
                }

                if (storeAddress != null && !storeAddress.trim().isEmpty()) {
                    updatedSeller.setStoreAddress(storeAddress);
                } else {
                    updatedSeller.setStoreAddress(existingSeller.getStoreAddress());
                }

                if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                    updatedSeller.setPaymentMethod(paymentMethod);
                } else {
                    updatedSeller.setPaymentMethod(existingSeller.getPaymentMethod());
                }

                // Handle profile image upload
                if (profileImage != null && !profileImage.isEmpty()) {
                    try {
                        // Validate profile image
                        ImageValidationUtil.ValidationResult imageValidation = ImageValidationUtil
                                .validateImageFile(profileImage, true);
                        if (!imageValidation.isValid()) {
                            return "redirect:/seller/settings?error=" + imageValidation.getErrorMessage();
                        }

                        String imagePath = saveProfileImage(profileImage);
                        updatedSeller.setProfileImagePath(imagePath);
                    } catch (IOException e) {
                        return "redirect:/seller/settings?error=Error uploading profile image: " + e.getMessage();
                    }
                } else {
                    updatedSeller.setProfileImagePath(existingSeller.getProfileImagePath());
                }

                updatedUser = updatedSeller;
            }

            // Update password if provided; otherwise ensure we don't pass the existing
            // encoded password for re-encoding
            if (newPassword != null && !newPassword.trim().isEmpty() &&
                    currentPassword != null && !currentPassword.trim().isEmpty()) {
                // Verify current password using raw currentPassword
                if (userService.login(user.getEmail(), currentPassword).isPresent()) {
                    updatedUser.setPassword(newPassword); // pass raw new password; service will encode
                } else {
                    return "redirect:/seller/settings?error=Current password is incorrect";
                }
            } else {
                // Avoid double-encoding by not sending an already-encoded password back to
                // service
                updatedUser.setPassword(null);
            }

            userService.updateUser(user.getUserId(), updatedUser);
            return "redirect:/seller/settings?success=Settings updated successfully";
        } catch (Exception e) {
            return "redirect:/seller/settings?error=Error updating settings: " + e.getMessage();
        }
    }

    // ---------------- ORDERS ----------------
    @GetMapping("/orders")
    public String orders(Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        List<Order> orders = orderService.getOrdersBySellerId(user.getUserId());
        model.addAttribute("orders", orders);
        model.addAttribute("user", user);
        return "seller_orders";
    }

    @PostMapping("/orders/ship/{id}")
    public String shipOrder(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";
        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty() || !orderOpt.get().getSellerId().equals(user.getUserId())) {
            return "redirect:/seller/orders?error=Order not found";
        }

        Order order = orderOpt.get();
        // Only ship if currently PENDING
        if (!"PENDING".equals(order.getStatus())) {
            return "redirect:/seller/orders?error=Only pending orders can be shipped";
        }

        orderService.shipOrder(id);
        // Record sale to seller's transactions as completed earnings
        transactionService.createSaleTransaction(user.getUserId(), order.getTotal());
        return "redirect:/seller/orders?success=Order shipped successfully";
    }

    @PostMapping("/orders/refund/{id}")
    public String refundOrder(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";
        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty() || !orderOpt.get().getSellerId().equals(user.getUserId())) {
            return "redirect:/seller/orders?error=Order not found";
        }

        Order order = orderOpt.get();
        // Allow refund from SHIPPED or COMPLETED; block if already REFUNDED
        if ("REFUNDED".equals(order.getStatus())) {
            return "redirect:/seller/orders?error=Order already refunded";
        }
        if (!java.util.Set.of("SHIPPED", "COMPLETED", "PENDING").contains(order.getStatus())) {
            return "redirect:/seller/orders?error=Order cannot be refunded";
        }

        orderService.refundOrder(id);
        // Record negative refund transaction to reduce earnings
        transactionService.createRefundTransaction(user.getUserId(), order.getTotal());
        return "redirect:/seller/orders?success=Order refunded successfully";
    }

    // ---------------- PRODUCTS ----------------
    @GetMapping("/products")
    public String products(Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        System.out.println("=== PRODUCTS DEBUG ===");
        System.out.println("User ID: " + user.getUserId());
        System.out.println("User Name: " + user.getName());
        System.out.println("User Role: " + user.getRole());

        // Debug: Check all products in database
        List<Product> allProducts = productService.getAllProducts();
        System.out.println("Total products in database: " + (allProducts != null ? allProducts.size() : 0));
        if (allProducts != null && !allProducts.isEmpty()) {
            for (Product product : allProducts) {
                System.out.println("All Products - Name: " + product.getName() + " (ID: " + product.getId()
                        + ", Seller ID: " + product.getSellerId() + ")");
            }
        } else {
            System.out.println("ERROR: No products found in database at all!");
        }

        List<Product> products = productService.getProductsBySellerId(user.getUserId());
        System.out.println("Found products count: " + (products != null ? products.size() : 0));

        if (products != null && !products.isEmpty()) {
            for (Product product : products) {
                System.out.println("Product: " + product.getName() + " (ID: " + product.getId() + ", Seller ID: "
                        + product.getSellerId() + ")");
            }
        } else {
            System.out.println("No products found for seller ID: " + user.getUserId());
        }

        model.addAttribute("products", products);
        model.addAttribute("user", user);
        return "seller_products";
    }

    @GetMapping("/products/available")
    public String availableProducts(Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        List<Product> products = productService.getAvailableProductsBySellerId(user.getUserId());
        model.addAttribute("products", products);
        model.addAttribute("user", user);
        return "seller_products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        model.addAttribute("user", user);
        model.addAttribute("product", new Product());
        return "SellerAddProduct";
    }

    @PostMapping("/products/add")
    public String addProduct(@RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "specifications", required = false, defaultValue = "") String specifications,
            @RequestParam("price") double price,
            @RequestParam("stock") int stock,
            @RequestParam("instrumentType") String instrumentType,
            @RequestParam("images") List<MultipartFile> imageFiles,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        try {
            System.out.println("Adding product: " + name);
            System.out.println("Image files count: " + (imageFiles != null ? imageFiles.size() : 0));

            // Validate image files before processing
            ImageValidationUtil.ValidationResult imageValidation = ImageValidationUtil.validateImageFiles(imageFiles,
                    false, 10);
            if (!imageValidation.isValid()) {
                return "redirect:/seller/products/add?error=" + imageValidation.getErrorMessage();
            }

            // Create Product object manually
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setSpecifications(specifications);
            product.setPrice(price);
            product.setStock(stock);
            product.setInstrumentType(instrumentType);
            product.setSellerId(user.getUserId());

            Product savedProduct = productService.addProduct(product, imageFiles);
            System.out.println("Product saved with ID: " + savedProduct.getId());
            return "redirect:/seller/products?success=Product added successfully";
        } catch (IOException e) {
            System.err.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/seller/products/add?error=Error uploading images: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Unexpected error adding product: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/seller/products/add?error=Error adding product: " + e.getMessage();
        }
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent() && product.get().getSellerId().equals(user.getUserId())) {
            model.addAttribute("product", product.get());
            model.addAttribute("user", user);
            return "update_product";
        }
        return "redirect:/seller/products?error=Product not found";
    }

    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "specifications", required = false, defaultValue = "") String specifications,
            @RequestParam("price") double price,
            @RequestParam("stock") int stock,
            @RequestParam("instrumentType") String instrumentType,
            @RequestParam(value = "images", required = false) List<MultipartFile> newImageFiles,
            @RequestParam(value = "primaryImageId", required = false) Long primaryImageId,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        try {
            System.out.println("=== UPDATE PRODUCT DEBUG ===");
            System.out.println("Updating product ID: " + id);
            System.out.println("Updated product name: " + name);
            System.out.println("Updated product description: " + description);
            System.out.println("Updated product price: " + price);
            System.out.println("Updated product stock: " + stock);
            System.out.println("Updated product instrument type: " + instrumentType);
            System.out.println("New image files count: " + (newImageFiles != null ? newImageFiles.size() : 0));

            // Validate new image files if provided
            if (newImageFiles != null && !newImageFiles.isEmpty()) {
                ImageValidationUtil.ValidationResult imageValidation = ImageValidationUtil
                        .validateImageFiles(newImageFiles, false, 10);
                if (!imageValidation.isValid()) {
                    return "redirect:/seller/products/edit/" + id + "?error=" + imageValidation.getErrorMessage();
                }
            }

            // Create updated Product object manually
            Product updatedProduct = new Product();
            updatedProduct.setName(name);
            updatedProduct.setDescription(description);
            updatedProduct.setSpecifications(specifications);
            updatedProduct.setPrice(price);
            updatedProduct.setStock(stock);
            updatedProduct.setInstrumentType(instrumentType);

            System.out.println("Calling productService.updateProductWithImages...");
            Product savedProduct = productService.updateProductWithImages(id, updatedProduct, newImageFiles);
            System.out.println(
                    "Product updated successfully, ID: " + (savedProduct != null ? savedProduct.getId() : "null"));

            // Handle primary image selection
            if (primaryImageId != null && primaryImageId > 0) {
                System.out.println("Setting primary image ID: " + primaryImageId);
                productService.setPrimaryImage(id, primaryImageId);
            }

            return "redirect:/seller/products?success=Product updated successfully";
        } catch (IOException e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/seller/products/edit/" + id + "?error=Error uploading images: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Unexpected error updating product: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/seller/products/edit/" + id + "?error=Error updating product: " + e.getMessage();
        }
    }

    @PostMapping("/products/remove-image/{imageId}")
    public ResponseEntity<String> removeImage(@PathVariable Long imageId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            System.out.println("Removing image with ID: " + imageId);
            productService.deleteImage(imageId);
            System.out.println("Image removed successfully");
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            System.err.println("Error removing image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent() && product.get().getSellerId().equals(user.getUserId())) {
            productService.deleteProduct(id);
            return "redirect:/seller/products?success=Product deleted successfully";
        }
        return "redirect:/seller/products?error=Product not found";
    }

    // ---------------- IMAGE MANAGEMENT ----------------
    @PostMapping("/products/images/set-primary/{imageId}")
    @ResponseBody
    public String setPrimaryImage(@PathVariable Long imageId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        try {
            // Get the product ID from the image
            // This would require a method to get product ID from image ID
            // For now, we'll implement a simplified version
            productService.setPrimaryImage(null, imageId); // You'll need to implement this properly
            return "{\"success\": true, \"message\": \"Primary image updated successfully\"}";
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"Error updating primary image: " + e.getMessage() + "\"}";
        }
    }

    @DeleteMapping("/products/images/delete/{imageId}")
    @ResponseBody
    public String deleteImage(@PathVariable Long imageId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        try {
            productService.deleteImage(imageId);
            return "{\"success\": true, \"message\": \"Image deleted successfully\"}";
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"Error deleting image: " + e.getMessage() + "\"}";
        }
    }

    // ---------------- ACCOUNT ----------------
    @GetMapping("/account")
    public String account(Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        Long sellerId = user.getUserId();
        List<Transaction> transactions = transactionService.getTransactionsBySellerId(sellerId);

        model.addAttribute("user", user);
        model.addAttribute("accountBalance", transactionService.getCompletedEarningsBySellerId(sellerId));
        model.addAttribute("totalEarnings", transactionService.getTotalEarningsBySellerId(sellerId));
        model.addAttribute("pendingPayouts", transactionService.getPendingEarningsBySellerId(sellerId));
        model.addAttribute("transactions", transactions);

        return "seller_account";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam double amount, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        Long sellerId = user.getUserId();
        double balance = transactionService.getCompletedEarningsBySellerId(sellerId);

        if (amount > 0 && amount <= balance) {
            transactionService.createWithdrawalRequest(sellerId, amount);
            return "redirect:/seller/account?success=Withdrawal request submitted successfully";
        } else {
            return "redirect:/seller/account?error=Invalid withdrawal amount";
        }
    }

    // ---------------- REVIEWS ----------------
    @GetMapping("/reviews")
    public String reviews(Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        Long sellerId = user.getUserId();
        System.out.println("=== SELLER REVIEWS CONTROLLER DEBUG ===");
        System.out.println("Seller ID: " + sellerId);

        List<Review> reviews = reviewService.getApprovedReviewsBySellerId(sellerId);
        System.out.println("Reviews found: " + (reviews != null ? reviews.size() : "NULL"));

        ReviewService.ReviewStats reviewStats = reviewService.getReviewStatsBySellerId(sellerId);
        System.out.println("ReviewStats object: " + (reviewStats != null ? "Created" : "NULL"));
        if (reviewStats != null) {
            System.out.println("Total Reviews: " + reviewStats.getTotalReviews());
            System.out.println("Average Rating: " + reviewStats.getAverageRating());
        }
        System.out.println("=== END SELLER REVIEWS CONTROLLER DEBUG ===");

        model.addAttribute("user", user);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewStats", reviewStats);
        return "seller_reviews";
    }

    @GetMapping("/reviews/product/{productId}")
    public String productReviews(@PathVariable Long productId, Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        // Verify the product belongs to this seller
        Optional<Product> product = productService.getProductById(productId);
        if (product.isEmpty() || !product.get().getSellerId().equals(user.getUserId())) {
            return "redirect:/seller/reviews?error=Product not found";
        }

        List<Review> reviews = reviewService.getApprovedReviewsByProductId(productId);
        double averageRating = reviewService.getAverageRatingByProductId(productId);
        long reviewCount = reviewService.getReviewCountByProductId(productId);

        model.addAttribute("user", user);
        model.addAttribute("product", product.get());
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviewCount", reviewCount);
        return "seller_product_reviews";
    }

    @PostMapping("/reviews/{reviewId}/approve")
    public String approveReview(@PathVariable Long reviewId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        reviewService.approveReview(reviewId);
        return "redirect:/seller/reviews?success=Review approved successfully";
    }

    @PostMapping("/reviews/{reviewId}/reject")
    public String rejectReview(@PathVariable Long reviewId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        reviewService.rejectReview(reviewId);
        return "redirect:/seller/reviews?success=Review rejected successfully";
    }

    @GetMapping("/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable Long reviewId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        reviewService.deleteReview(reviewId);
        return "redirect:/seller/reviews?success=Review deleted successfully";
    }

    // ---------------- ACCOUNT DELETION ----------------
    @PostMapping("/deleteAccount")
    public String deleteAccount(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (!isSeller(user))
            return "redirect:/users/login?error=Please log in as a seller";

        try {
            Long sellerId = user.getUserId();

            // Delete all seller-related data
            // 1. Delete all products and their images
            List<Product> products = productService.getProductsBySellerId(sellerId);
            for (Product product : products) {
                productService.deleteProduct(product.getId());
            }

            // 2. Delete all orders related to this seller
            List<Order> orders = orderService.getOrdersBySellerId(sellerId);
            for (Order order : orders) {
                orderService.deleteOrder(order.getId());
            }

            // 3. Delete all transactions
            List<Transaction> transactions = transactionService.getTransactionsBySellerId(sellerId);
            for (Transaction transaction : transactions) {
                transactionService.deleteTransaction(transaction.getId());
            }

            // 4. Delete all reviews for this seller's products
            List<Review> reviews = reviewService.getReviewsBySellerId(sellerId);
            for (Review review : reviews) {
                reviewService.deleteReview(review.getId());
            }

            // 5. Finally, delete the user account completely (hard delete)
            userService.deleteUser(sellerId);

            // Redirect to login with success message
            return "redirect:/users/login?success=Account deleted successfully";

        } catch (Exception e) {
            return "redirect:/seller/settings?error=Error deleting account: " + e.getMessage();
        }
    }

    // Helper method to save profile images
    private String saveProfileImage(MultipartFile file) throws IOException {
        String uploadDir = "uploads/profile/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            originalFileName = "profile";
        }
        String fileExtension = originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : ".jpg";
        String fileName = "profile_" + UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}