package com.app.musicstore.controller;

import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard/admin")
    public String adminDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "admin-dashboard";
    }

    @GetMapping("/dashboard/artist")
    public String artistDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "artist-dashboard";
    }

    @GetMapping("/dashboard/item-seller")
    public String itemSellerDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "item-seller-dashboard";
    }

    @GetMapping("/dashboard/course-seller")
    public String courseSellerDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "course-seller-dashboard";
    }

    @GetMapping("/dashboard/customer")
    public String customerDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "customer-dashboard";
    }

    /**
     * Helper method to get the authenticated user from SecurityContext
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser(); // This will return the User object from CustomUserDetails
        }

        return null;
    }
}