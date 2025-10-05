package com.app.musicstore.controller;

import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard/{role}")
    public String dashboard(@PathVariable String role, Model model) {
        User user = getFreshAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("user", user);

        String normalizedRole = role.toLowerCase();

        switch (normalizedRole) {
            case "admin":
                model.addAttribute("totalUsers", userService.getTotalUsersCount());
                model.addAttribute("activeUsers", userService.getActiveUsersCount());
                return "admin-dashboard";
            case "artist":
                return "artist-dashboard";
            case "item-seller":
                return "item-seller-dashboard";
            case "course-seller":
                return "course-seller-dashboard";
            case "customer":
                return "customer-dashboard";
            default:
                // fallback if unknown role
                return "redirect:/";
        }
    }

    private User getFreshAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUser().getUserId();

            return userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        return null;
    }
}