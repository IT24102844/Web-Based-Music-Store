package com.app.musicstore.controller;

import com.app.musicstore.model.Song;
import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.SongService;
import com.app.musicstore.service.UnifiedPaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerSongController {

    private final SongService songService;
    private final UnifiedPaymentService unifiedPaymentService;

    public CustomerSongController(SongService songService, UnifiedPaymentService unifiedPaymentService) {
        this.songService = songService;
        this.unifiedPaymentService = unifiedPaymentService;
    }

    // Helper method to get authenticated user
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

    // Customer Songs Dashboard - Main page
    @GetMapping("/songs")
    public String showCustomerSongsDashboard(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            Model model) {

        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/users/login";
        }

        List<Song> songs;

        // search or filter
        if (search != null && !search.trim().isEmpty()) {
            songs = songService.searchSongs(search);
        } else if (genre != null && !genre.trim().isEmpty()) {
            songs = songService.getSongsByGenre(genre);
        } else {
            songs = songService.getAllSongs();
        }

        // genres for filter dropdown
        List<String> genres = songService.getAllGenres();

        model.addAttribute("songs", songs);
        model.addAttribute("genres", genres);
        model.addAttribute("user", currentUser);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedGenre", genre);

        return "customer-songs-dashboard";
    }

    // View Song Details (for customers)
    @GetMapping("/songs/{id}")
    public String viewSongDetails(
            @PathVariable Long id,
            @RequestParam(required = false) Long unifiedPaymentId,
            @RequestParam(required = false) String success,
            Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/users/login";
        }

        Optional<Song> song = songService.getSongById(id);
        if (song.isEmpty()) {
            return "redirect:/customer/songs?error=Song not found";
        }

        // Check if artist is loaded
        Song songObj = song.get();
        if (songObj.getArtist() == null) {
            return "redirect:/customer/songs?error=Artist information not available for this song";
        }

        // Check if user has purchased this song
        boolean hasPurchased = unifiedPaymentService.hasUserPurchasedSong(currentUser, id);

        model.addAttribute("song", songObj);
        model.addAttribute("user", currentUser);
        model.addAttribute("hasPurchased", hasPurchased);

        // Show success message after payment
        if (unifiedPaymentId != null || success != null) {
            model.addAttribute("success", "Payment successful! You can now download the song.");
        }

        return "customer-songs-details";
    }

    // Redirect to unified payment checkout for a song
    @PostMapping("/songs/{id}/purchase")
    public String purchaseSong(@PathVariable Long id, Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/users/login";
        }

        var songOpt = songService.getSongById(id);
        if (songOpt.isEmpty()) {
            return "redirect:/customer/songs?error=Song not found";
        }
        Song song = songOpt.get();

        // Check if already purchased
        if (unifiedPaymentService.hasUserPurchasedSong(currentUser, id)) {
            return "redirect:/customer/songs/" + id + "?error=You have already purchased this song";
        }

        String itemType = "SONG";
        Long itemId = song.getId();
        String itemName = song.getName();
        Double amount = song.getPrice() != null ? song.getPrice() : 0.0;

        // Set success redirect to come back to song details page
        String successRedirect = "/customer/songs/" + id;

        String url = String.format(
                "redirect:/payments/checkout?itemType=%s&itemId=%d&itemName=%s&amount=%s&successRedirect=%s",
                java.net.URLEncoder.encode(itemType, java.nio.charset.StandardCharsets.UTF_8),
                itemId,
                java.net.URLEncoder.encode(itemName, java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(String.valueOf(amount), java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(successRedirect, java.nio.charset.StandardCharsets.UTF_8));
        return url;
    }
}