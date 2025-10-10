package com.app.musicstore.controller;

import com.app.musicstore.model.Artist;
import com.app.musicstore.model.Ticket;
import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.EventService;
import com.app.musicstore.service.ArtistService;
import com.app.musicstore.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private EventService eventService;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private PaymentService paymentService;

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

        // Get artist events count and artist details
        Optional<Artist> artistOpt = artistService.findByUserId(user.getUserId());
        if (artistOpt.isPresent()) {
            Artist artist = artistOpt.get();
            int eventsCount = eventService.getArtistEvents(artist).size();
            model.addAttribute("eventsCount", eventsCount);
            model.addAttribute("artist", artist);
            System.out.println("🎭 Loaded artist for dashboard - Stage: " + artist.getStageName() + ", Genre: " + artist.getGenre());
        } else {
            model.addAttribute("eventsCount", 0);
            System.out.println("⚠️ No artist profile found for dashboard user: " + user.getUserId());
        }

        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
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

        // Get user's tickets count for events attended
        List<Ticket> userTickets = paymentService.getUserTickets(user);
        long eventsAttendedCount = userTickets.stream()
                .filter(ticket -> "ACTIVE".equals(ticket.getStatus()) || "USED".equals(ticket.getStatus()))
                .count();

        System.out.println("🎫 Customer dashboard - User: " + user.getEmail() + ", Tickets: " + eventsAttendedCount);

        model.addAttribute("user", user);
        model.addAttribute("eventsAttendedCount", eventsAttendedCount);
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