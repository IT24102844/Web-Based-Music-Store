package com.app.musicstore.controller;

import com.app.musicstore.model.User;
import com.app.musicstore.service.SessionUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private SessionUserService sessionUserService;

    @GetMapping("/dashboard/admin")
    public String adminDashboard(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "admin-dashboard";
    }

    @GetMapping("/dashboard/artist")
    public String artistDashboard(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "artist-dashboard";
    }

    @GetMapping("/dashboard/item-seller")
    public String itemSellerDashboard(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "seller_dashboard";
    }

    @GetMapping("/dashboard/course-seller")
    public String courseSellerDashboard(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "seller_dashboard";
    }

    @GetMapping("/dashboard/customer")
    public String customerDashboard(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "customer-dashboard";
    }

}