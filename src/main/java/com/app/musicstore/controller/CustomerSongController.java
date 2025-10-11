package com.app.musicstore.controller;

import com.app.musicstore.model.Song;
import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.SongService;
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

    public CustomerSongController(SongService songService) {
        this.songService = songService;
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

        //  search or filter
        if (search != null && !search.trim().isEmpty()) {
            songs = songService.searchSongs(search);
        } else if (genre != null && !genre.trim().isEmpty()) {
            songs = songService.getSongsByGenre(genre);
        } else {
            songs = songService.getAllSongs();
        }

        //  genres for filter dropdown
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
    public String viewSongDetails(@PathVariable Long id, Model model) {
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

        model.addAttribute("song", songObj);
        model.addAttribute("user", currentUser);
        return "customer-songs-details";
    }

    // Handle purchase request (placeholder for now)
    @PostMapping("/songs/{id}/purchase")
    public String purchaseSong(@PathVariable Long id, Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/users/login";
        }


        // For now, just redirect with a success message
        return "redirect:/customer/songs?success=Purchase functionality coming soon! Song ID: " + id;
    }
}