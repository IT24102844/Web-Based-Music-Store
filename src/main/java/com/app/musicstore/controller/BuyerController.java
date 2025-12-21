package com.app.musicstore.controller;

import com.app.musicstore.model.*;
import com.app.musicstore.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/buyer")
public class BuyerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private SessionUserService sessionUserService;

    // Customer Dashboard - Product Browsing
    @GetMapping("/dashboard")
    public String customerDashboard(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        // Get all available products
        List<Product> products = productService.getAllProducts();
        model.addAttribute("user", user);
        model.addAttribute("products", products);

        // Average rating and review count per product for listing cards (batch to avoid
        // N+1)
        List<Long> productIds = products.stream().map(Product::getId).toList();
        java.util.Map<Long, Double> averageByProduct = reviewService.getAverageRatingsForProducts(productIds);
        java.util.Map<Long, Long> countByProduct = reviewService.getReviewCountsForProducts(productIds);
        model.addAttribute("averageByProduct", averageByProduct);
        model.addAttribute("countByProduct", countByProduct);

        // Get unique instrument types for filter
        List<String> instrumentTypes = products.stream()
                .map(Product::getInstrumentType)
                .distinct()
                .toList();
        model.addAttribute("instrumentTypes", instrumentTypes);

        return "customer-instrument-dashboard";
    }

    // Search and Filter Products
    @GetMapping("/products")
    public String searchProducts(@RequestParam(value = "search", required = false) String search,
                                 @RequestParam(value = "instrumentType", required = false) String instrumentType,
                                 @RequestParam(value = "minPrice", required = false) Double minPrice,
                                 @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                                 Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        List<Product> products = productService.getAllProducts();

        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            String s = search.toLowerCase();
            products = products.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(s)) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(s)))
                    .toList();
        }

        if (instrumentType != null && !instrumentType.trim().isEmpty()) {
            String it = instrumentType.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getInstrumentType() != null && p.getInstrumentType().toLowerCase().equals(it))
                    .toList();
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() >= minPrice)
                    .toList();
        }

        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() <= maxPrice)
                    .toList();
        }

        model.addAttribute("user", user);
        model.addAttribute("products", products);
        // Batch ratings for list
        List<Long> pids = products.stream().map(Product::getId).toList();
        model.addAttribute("averageByProduct", reviewService.getAverageRatingsForProducts(pids));
        model.addAttribute("countByProduct", reviewService.getReviewCountsForProducts(pids));
        model.addAttribute("search", search);
        model.addAttribute("selectedInstrumentType", instrumentType);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        // Get unique instrument types for filter dropdown
        List<String> instrumentTypes = productService.getAllProducts().stream()
                .map(Product::getInstrumentType)
                .distinct()
                .toList();
        model.addAttribute("instrumentTypes", instrumentTypes);

        return "customer-products";
    }

    // Product Detail View
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/buyer/dashboard?error=Product not found";
        }

        Product product = productOpt.get();

        // Get seller information
        Optional<User> sellerOpt = userService.getUserById(product.getSellerId());
        User seller = sellerOpt.orElse(null);

        // Get product reviews
        List<Review> reviews = reviewService.getReviewsByProductId(id);

        // Calculate average rating
        double averageRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        model.addAttribute("user", user);
        model.addAttribute("product", product);
        model.addAttribute("seller", seller);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviewCount", reviews.size());

        return "customer-product-detail";
    }

    // Add to Cart (placeholder for future implementation)
    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        // Persist to DB cart
        cartService.addToCart(user.getUserId(), productId, quantity);

        return "redirect:/buyer/cart?success=Added to cart";
    }

    // View cart page
    @GetMapping("/cart")
    public String viewCart(Model model,
                           HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        var dbItems = cartService.getCartItems(user.getUserId());
        List<CartViewItem> items = dbItems.stream()
                .map(ci -> new CartViewItem(ci.getProduct(), ci.getQuantity()))
                .toList();

        double total = dbItems.stream().mapToDouble(ci -> ci.getProduct().getPrice() * ci.getQuantity()).sum();

        model.addAttribute("user", user);
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }

    // Remove item from cart
    @PostMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId,
                                 HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        cartService.removeFromCart(user.getUserId(), productId);
        return "redirect:/buyer/cart?success=Item removed";
    }

    // Update quantities
    @PostMapping("/cart/update")
    public String updateCart(@RequestParam("productId") List<Long> productIds,
                             @RequestParam("quantity") List<Integer> quantities,
                             HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        cartService.updateQuantities(user.getUserId(), productIds, quantities);
        return "redirect:/buyer/cart?success=Cart updated";
    }

    // Checkout selected products from cart → unified payment
    @PostMapping("/cart/checkout")
    public String checkoutFromCart(@RequestParam(value = "selectedIds", required = false) List<Long> selectedIds,
                                   HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        if (selectedIds == null || selectedIds.isEmpty()) {
            return "redirect:/buyer/cart?error=Please select at least one item";
        }

        // Calculate total from selected items
        var dbItems = cartService.getCartItems(user.getUserId());
        java.util.Map<Long, Integer> qtyByProduct = dbItems.stream()
                .collect(java.util.stream.Collectors.toMap(ci -> ci.getProduct().getId(), ci -> ci.getQuantity()));

        double total = selectedIds.stream()
                .filter(qtyByProduct::containsKey)
                .mapToDouble(pid -> {
                    var product = productService.getProductById(pid).orElse(null);
                    return product != null ? product.getPrice() * qtyByProduct.get(pid) : 0.0;
                })
                .sum();

        // Store selected IDs and quantities in session for after payment processing
        String selectedIdsCsv = selectedIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));

        String quantitiesCsv = selectedIds.stream()
                .map(pid -> String.valueOf(qtyByProduct.get(pid)))
                .collect(java.util.stream.Collectors.joining(","));

        // Store in session for after-payment processing
        session.setAttribute("pendingCartItems_" + user.getUserId(), selectedIdsCsv);
        session.setAttribute("pendingCartQuantities_" + user.getUserId(), quantitiesCsv);

        // Redirect to unified checkout - treat as single payment with calculated total
        String successRedirect = "/buyer/after-unified-cart";
        String url = String.format(
                "redirect:/payments/checkout?itemType=%s&itemId=%d&itemName=%s&amount=%s&successRedirect=%s",
                java.net.URLEncoder.encode("INSTRUMENT_CART", java.nio.charset.StandardCharsets.UTF_8),
                0L, // Using 0 as itemId for cart checkout
                java.net.URLEncoder.encode("Cart with " + selectedIds.size() + " items", java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(String.valueOf(total), java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(successRedirect, java.nio.charset.StandardCharsets.UTF_8));
        return url;
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam("selectedIds") List<Long> selectedIds,
                             HttpSession session,
                             Model model) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        // Build per-seller totals and create orders per seller
        var dbItems = cartService.getCartItems(user.getUserId());
        java.util.Map<Long, Integer> qtyByProduct = dbItems.stream()
                .collect(java.util.stream.Collectors.toMap(ci -> ci.getProduct().getId(), ci -> ci.getQuantity()));

        // Group by sellerId
        java.util.Map<Long, List<Product>> bySeller = new java.util.HashMap<>();
        for (Long pid : selectedIds) {
            var product = productService.getProductById(pid).orElse(null);
            if (product == null)
                continue;
            bySeller.computeIfAbsent(product.getSellerId(), k -> new java.util.ArrayList<>()).add(product);
        }

        // Create orders per seller with items
        for (var entry : bySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<Product> sellerProducts = entry.getValue();
            orderService.createOrderWithItems(sellerId, user.getUserId(), user.getName(), sellerProducts, qtyByProduct);
        }

        // Remove selected items from cart after placing order
        for (Long pid : selectedIds) {
            cartService.removeFromCart(user.getUserId(), pid);
        }

        return "redirect:/payments/pay";
    }

    @GetMapping("/orders")
    public String myOrders(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        List<Order> orders = orderService.getOrdersByCustomerId(user.getUserId());
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        return "buyer_orders";
    }

    @GetMapping("/reviews")
    public String myReviews(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        // Only allow reviewing items from completed or shipped orders
        List<String> reviewableStatuses = List.of("COMPLETED", "SHIPPED");
        List<Order> orders = orderService.getOrdersByCustomerId(user.getUserId())
                .stream()
                .filter(o -> reviewableStatuses.contains(o.getStatus()))
                .toList();

        // Attach existing review (if any) per order item for convenience in template
        // Prefill existing reviews map: productId -> Review
        java.util.Map<Long, Review> existingByProduct = new java.util.HashMap<>();
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                if (!existingByProduct.containsKey(item.getProductId())) {
                    Review existing = reviewService.getByProductAndCustomer(item.getProductId(), user.getUserId());
                    if (existing != null)
                        existingByProduct.put(item.getProductId(), existing);
                }
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("existingReviews", existingByProduct);
        return "buyer_reviews";
    }

    @PostMapping("/reviews")
    public String submitReview(@RequestParam Long productId,
                               @RequestParam Integer rating,
                               @RequestParam(required = false) String comment,
                               HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        // Validate rating
        if (rating == null || rating < 1 || rating > 5) {
            return "redirect:/buyer/reviews?error=Please select a valid rating (1-5 stars)";
        }

        // Verify the user purchased the product from a completed/shipped order
        boolean purchased = orderService.getOrdersByCustomerId(user.getUserId()).stream()
                .filter(o -> java.util.Set.of("COMPLETED", "SHIPPED").contains(o.getStatus()))
                .flatMap(o -> o.getItems().stream())
                .anyMatch(oi -> oi.getProductId().equals(productId));
        if (!purchased) {
            return "redirect:/buyer/reviews?error=You can only review products you purchased";
        }

        // Check if product exists
        if (!productService.getProductById(productId).isPresent()) {
            return "redirect:/buyer/reviews?error=Product not found";
        }

        // Create review
        Review review = new Review();
        review.setProductId(productId);
        review.setCustomerId(user.getUserId());
        review.setCustomerName(user.getName());
        review.setCustomerEmail(user.getEmail());
        review.setRating(rating);
        review.setComment(comment != null && comment.trim().isEmpty() ? null : comment);
        review.setIsVerifiedPurchase(true);

        try {
            reviewService.createReview(review);
            return "redirect:/buyer/reviews?success=Thank you for your review!";
        } catch (IllegalArgumentException ex) {
            return "redirect:/buyer/reviews?error="
                    + java.net.URLEncoder.encode(ex.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            System.err.println("Error creating review: " + ex.getMessage());
            ex.printStackTrace();
            return "redirect:/buyer/reviews?error=Failed to submit review. Please try again.";
        }
    }

    // simple immutable holder for cart view
    public record CartViewItem(Product product, int quantity) {
    }

    // Checkout selected products → unified payment
    @PostMapping("/checkout")
    public String checkoutSelected(@RequestParam(value = "selectedIds", required = false) List<Long> selectedIds,
                                   Model model,
                                   HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        if (selectedIds == null || selectedIds.isEmpty()) {
            return "redirect:/buyer/products?error=Please select at least one product";
        }

        List<Product> selectedProducts = selectedIds.stream()
                .map(productService::getProductById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        double total = selectedProducts.stream()
                .mapToDouble(Product::getPrice)
                .sum();

        // Redirect to unified checkout as a single cart purchase item
        String itemType = "INSTRUMENT_CART";
        String itemName = "Cart Checkout (" + selectedProducts.size() + " items)";
        String successRedirect = "/buyer/after-unified-cart?selectedIds="
                + selectedIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        String url = String.format(
                "redirect:/payments/checkout?itemType=%s&itemId=%d&itemName=%s&amount=%s&successRedirect=%s",
                java.net.URLEncoder.encode(itemType, java.nio.charset.StandardCharsets.UTF_8),
                0L,
                java.net.URLEncoder.encode(itemName, java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(String.valueOf(total), java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(successRedirect, java.nio.charset.StandardCharsets.UTF_8));
        return url;
    }

    // After unified payment for cart → place orders and clear items
    @GetMapping("/after-unified-cart")
    public String afterUnifiedCart(@RequestParam(value = "unifiedPaymentId", required = false) Long unifiedPaymentId,
                                   HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (!isCustomer(user))
            return "redirect:/users/login?error=Please log in as a customer";

        // Retrieve selected IDs and quantities from session
        String selectedIdsCsv = (String) session.getAttribute("pendingCartItems_" + user.getUserId());
        String quantitiesCsv = (String) session.getAttribute("pendingCartQuantities_" + user.getUserId());

        // Clean up session
        session.removeAttribute("pendingCartItems_" + user.getUserId());
        session.removeAttribute("pendingCartQuantities_" + user.getUserId());

        if (selectedIdsCsv == null || selectedIdsCsv.isEmpty()) {
            return "redirect:/buyer/cart?error=No pending cart items found";
        }

        List<Long> selectedIds = java.util.Arrays.stream(selectedIdsCsv.split(","))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .toList();

        List<Integer> quantities = java.util.Arrays.stream(quantitiesCsv.split(","))
                .filter(s -> !s.isBlank())
                .map(Integer::valueOf)
                .toList();

        // Create quantity map
        java.util.Map<Long, Integer> qtyByProduct = new java.util.HashMap<>();
        for (int i = 0; i < selectedIds.size(); i++) {
            qtyByProduct.put(selectedIds.get(i), quantities.get(i));
        }

        // Group by seller and create orders
        java.util.Map<Long, List<Product>> bySeller = new java.util.HashMap<>();
        for (Long pid : selectedIds) {
            var product = productService.getProductById(pid).orElse(null);
            if (product == null)
                continue;
            bySeller.computeIfAbsent(product.getSellerId(), k -> new java.util.ArrayList<>()).add(product);
        }

        for (var entry : bySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<Product> sellerProducts = entry.getValue();
            orderService.createOrderWithItems(sellerId, user.getUserId(), user.getName(), sellerProducts, qtyByProduct);
        }

        // Remove selected items from cart
        for (Long pid : selectedIds) {
            cartService.removeFromCart(user.getUserId(), pid);
        }

        return "redirect:/buyer/orders?success=Payment successful. Order placed.";
    }

    // Helper methods

    private boolean isCustomer(User user) {
        // Allow both customers AND sellers to access customer features (dual access)
        return user != null && (user.getRole() == Role.CUSTOMER ||
                user.getRole() == Role.ITEM_SELLER ||
                user.getRole() == Role.COURSE_SELLER);
    }
}
