package com.app.musicstore.controller;

import com.app.musicstore.model.Product;
import com.app.musicstore.service.ProductService;
import com.app.musicstore.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/")
    public String landingPage() {
        return "landing";
    }

    @GetMapping("/public_products")
    public String publicProducts(@RequestParam(value = "search", required = false) String search,
                                 @RequestParam(value = "instrumentType", required = false) String instrumentType,
                                 @RequestParam(value = "sort", required = false) String sort,
                                 Model model) {
        List<Product> products = productService.getAllProducts();
        if (search != null && !search.trim().isEmpty()) {
            String s = search.toLowerCase();
            products = products.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(s)) ||
                                 (p.getDescription() != null && p.getDescription().toLowerCase().contains(s)))
                    .toList();
        }
        if (instrumentType != null && !instrumentType.trim().isEmpty()) {
            products = products.stream()
                    .filter(p -> instrumentType.equalsIgnoreCase(p.getInstrumentType()))
                    .toList();
        }
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price_asc" -> products = products.stream()
                        .sorted(java.util.Comparator.comparingDouble(Product::getPrice))
                        .toList();
                case "price_desc" -> products = products.stream()
                        .sorted(java.util.Comparator.comparingDouble(Product::getPrice).reversed())
                        .toList();
                case "name_asc" -> products = products.stream()
                        .sorted(java.util.Comparator.comparing(p -> p.getName() == null ? "" : p.getName().toLowerCase()))
                        .toList();
                case "name_desc" -> products = products.stream()
                        .sorted(java.util.Comparator.comparing((Product p) -> p.getName() == null ? "" : p.getName().toLowerCase()).reversed())
                        .toList();
            }
        }
        model.addAttribute("products", products);
        // Batch averages/counts for public listing to avoid N+1
        List<Long> productIds = products.stream().map(Product::getId).toList();
        model.addAttribute("averageByProduct", reviewService.getAverageRatingsForProducts(productIds));
        model.addAttribute("countByProduct", reviewService.getReviewCountsForProducts(productIds));
        model.addAttribute("search", search);
        model.addAttribute("instrumentType", instrumentType);
        model.addAttribute("sort", sort);
        return "public_products";
    }

    @GetMapping("/public_products/{id}")
    public String publicProductDetail(@PathVariable Long id, Model model) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/public_products?error=Product not found";
        }
        Product product = productOpt.get();
        model.addAttribute("product", product);
        model.addAttribute("averageRating", reviewService.getAverageRatingByProductId(id));
        model.addAttribute("reviewCount", reviewService.getReviewCountByProductId(id));
        // Include approved reviews for public view
        model.addAttribute("reviews", reviewService.getApprovedReviewsByProductId(id));
        return "public_product_detail";
    }

    @GetMapping("/session-check")
    public String sessionCheck() {
        return "session-check";
    }
}
