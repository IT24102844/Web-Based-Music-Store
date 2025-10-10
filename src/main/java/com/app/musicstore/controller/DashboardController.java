package com.app.musicstore.controller;

import com.app.musicstore.model.Artist;
import com.app.musicstore.model.User;
import com.app.musicstore.model.Song;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.ArtistService;
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

    public DashboardController(ArtistService artistService, SongService songService) {
        this.artistService = artistService;
        this.songService = songService;
    }

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