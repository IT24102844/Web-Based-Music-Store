package com.app.musicstore.controller;

import com.app.musicstore.model.Artist;
import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.EventService;
import com.app.musicstore.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private EventService eventService;

    @Autowired
    private ArtistService artistService;

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

        // Get artist events count using your service method
        Optional<Artist> artistOpt = artistService.findByUserId(user.getUserId());
        if (artistOpt.isPresent()) {
            int eventsCount = eventService.getArtistEvents(artistOpt.get()).size();
            model.addAttribute("eventsCount", eventsCount);
        } else {
            model.addAttribute("eventsCount", 0);
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
            return userDetails.getUser();
        }

        return null;
    }
}