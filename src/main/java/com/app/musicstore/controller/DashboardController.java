package com.app.musicstore.controller;

import com.app.musicstore.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard/admin")
    public String adminDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "admin-dashboard";
    }

    @GetMapping("/dashboard/artist")
    public String artistDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "artist-dashboard";
    }

    @GetMapping("/dashboard/item-seller")
    public String itemSellerDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "item-seller-dashboard";
    }

    @GetMapping("/dashboard/course-seller")
    public String courseSellerDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "course-seller-dashboard";
    }

    @GetMapping("/dashboard/customer")
    public String customerDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "customer-dashboard";
    }
}
