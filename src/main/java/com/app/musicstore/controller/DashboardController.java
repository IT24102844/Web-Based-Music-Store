package com.app.musicstore.controller;

import com.app.musicstore.model.Artist;
import com.app.musicstore.model.Song;
import com.app.musicstore.model.Ticket;
import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.ArtistService;
import com.app.musicstore.service.EventService;
import com.app.musicstore.service.PaymentService;
import com.app.musicstore.service.SongService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final ArtistService artistService;
    private final SongService songService;
    private final EventService eventService;
    private final PaymentService paymentService;

    public DashboardController(ArtistService artistService,
                               SongService songService,
                               EventService eventService,
                               PaymentService paymentService) {
        this.artistService = artistService;
        this.songService = songService;
        this.eventService = eventService;
        this.paymentService = paymentService;
    }

    // -------------------- ADMIN DASHBOARD --------------------
    @GetMapping("/dashboard/admin")
    public String adminDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "admin-dashboard";
    }

    // -------------------- ARTIST DASHBOARD --------------------
    @GetMapping("/dashboard/artist")
    public String artistDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }

        Optional<Artist> artistOpt = artistService.findByUserId(user.getUserId());
        if (artistOpt.isPresent()) {
            Artist artist = artistOpt.get();
            List<Song> songs = songService.getSongsByArtist(artist);
            int songCount = songs.size();
            int eventsCount = eventService.getArtistEvents(artist).size();

            model.addAttribute("artist", artist);
            model.addAttribute("songCount", songCount);
            model.addAttribute("eventsCount", eventsCount);
            System.out.println("🎭 Loaded artist dashboard: " + artist.getStageName());
        } else {
            model.addAttribute("songCount", 0);
            model.addAttribute("eventsCount", 0);
            System.out.println("⚠️ No artist profile found for user: " + user.getUserId());
        }

        model.addAttribute("followerCount", 0); // Placeholder for now
        model.addAttribute("rating", 0.0);
        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);

        return "artist-dashboard";
    }

    // -------------------- ITEM SELLER DASHBOARD --------------------
    @GetMapping("/dashboard/item-seller")
    public String itemSellerDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "seller_dashboard";
    }

    // -------------------- COURSE SELLER DASHBOARD --------------------
    @GetMapping("/dashboard/course-seller")
    public String courseSellerDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "course-seller-dashboard";
    }

    // -------------------- CUSTOMER DASHBOARD --------------------
    @GetMapping("/dashboard/customer")
    public String customerDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }

        List<Song> allSongs = songService.getAllSongs();
        List<String> genres = songService.getAllGenres();

        int totalSongs = allSongs.size();
        int totalGenres = genres.size();
        int totalArtists = (int) allSongs.stream()
                .map(song -> song.getArtist().getUserId())
                .distinct()
                .count();

        List<Ticket> userTickets = paymentService.getUserTickets(user);
        long eventsAttendedCount = userTickets.stream()
                .filter(ticket -> "ACTIVE".equals(ticket.getStatus()) || "USED".equals(ticket.getStatus()))
                .count();

        System.out.println("🎫 Customer dashboard - User: " + user.getEmail() + ", Tickets: " + eventsAttendedCount);

        model.addAttribute("user", user);
        model.addAttribute("totalSongs", totalSongs);
        model.addAttribute("totalGenres", totalGenres);
        model.addAttribute("totalArtists", totalArtists);
        model.addAttribute("genres", genres);
        model.addAttribute("eventsAttendedCount", eventsAttendedCount);

        return "customer-dashboard";
    }

    // -------------------- AUTH HELPER --------------------
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        return null;
    }
}
