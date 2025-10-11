package com.app.musicstore.controller;

import com.app.musicstore.model.User;
import com.app.musicstore.service.SessionUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import com.app.musicstore.model.Artist;
import com.app.musicstore.model.Ticket;
import com.app.musicstore.model.User;
import com.app.musicstore.model.Song;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.ArtistService;
import com.app.musicstore.service.SongService;
import com.app.musicstore.service.EventService;
import com.app.musicstore.service.ArtistService;
import com.app.musicstore.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final ArtistService artistService;
    private final SongService songService;

    public DashboardController(ArtistService artistService, SongService songService) {
        this.artistService = artistService;
        this.songService = songService;
    }
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
    public String artistDashboard(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
    public String artistDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }

        // Get artist information
        Optional<Artist> artistOpt = artistService.findByUserId(user.getUserId());
        if (artistOpt.isPresent()) {
            Artist artist = artistOpt.get();

            // Get song statistics
            List<Song> songs = songService.getSongsByArtist(artist);
            int songCount = songs.size();
            model.addAttribute("songCount", songCount);

        } else {
            model.addAttribute("songCount", 0);
        }

        // Placeholder for events
        model.addAttribute("eventsCount", 0);

        // Placeholder statistics
        model.addAttribute("followerCount", 0);
        model.addAttribute("rating", 0.0);

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
    public String itemSellerDashboard(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
    public String itemSellerDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "seller_dashboard";
    }

    @GetMapping("/dashboard/course-seller")
    public String courseSellerDashboard(Model model, HttpSession session) {
        User user = sessionUserService.getAuthenticatedUser(session);
        return "item-seller-dashboard";
    }

    @GetMapping("/dashboard/course-seller")
    public String courseSellerDashboard(Model model) {
        User user = getAuthenticatedUser();
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

        return "course-seller-dashboard";
    }

    @GetMapping("/dashboard/customer")
    public String customerDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }

        // Add customer-specific statistics
        List<Song> allSongs = songService.getAllSongs();
        List<String> genres = songService.getAllGenres();

        int totalSongs = allSongs.size();
        int totalGenres = genres.size();
        int totalArtists = (int) allSongs.stream()
                .map(song -> song.getArtist().getUserId())
                .distinct()
                .count();

        model.addAttribute("user", user);
        model.addAttribute("totalSongs", totalSongs);
        model.addAttribute("totalGenres", totalGenres);
        model.addAttribute("totalArtists", totalArtists);
        model.addAttribute("genres", genres);

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