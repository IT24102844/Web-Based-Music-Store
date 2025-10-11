package com.app.musicstore.controller;

import com.app.musicstore.model.Song;
import com.app.musicstore.model.Artist;
import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.SongService;
import com.app.musicstore.service.ArtistService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/artist/songs")
public class SongController {

    private final SongService songService;
    private final ArtistService artistService;

    public SongController(SongService songService, ArtistService artistService) {
        this.songService = songService;
        this.artistService = artistService;
    }

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

    // helper method to get current artist using Spring Security
    private Artist getCurrentArtist() {
        User user = getAuthenticatedUser();
        if (user == null || !user.getRole().name().equals("ARTIST")) {
            return null;
        }

        return artistService.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Artist profile not found for user: " + user.getUserId()));
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        Artist currentArtist = getCurrentArtist();
        if (currentArtist == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        model.addAttribute("song", new Song());
        model.addAttribute("user", currentArtist);
        return "song-upload-form";
    }


    @PostMapping("/upload")
    public String uploadSong(
            @RequestParam("name") String name,
            @RequestParam("genre") String genre,
            @RequestParam("price") Double price,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("previewClip") MultipartFile previewClip,
            @RequestParam("songImage") MultipartFile songImage,
            Model model) {

        Artist currentArtist = getCurrentArtist();
        if (currentArtist == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        // VALIDATION CHECKS
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("error", "Please enter a song name");
            model.addAttribute("user", currentArtist);
            return "song-upload-form";
        }

        if (genre == null || genre.trim().isEmpty()) {
            model.addAttribute("error", "Please enter a genre");
            model.addAttribute("user", currentArtist);
            return "song-upload-form";
        }

        if (price == null || price < 0) {
            model.addAttribute("error", "Please enter a valid price");
            model.addAttribute("user", currentArtist);
            return "song-upload-form";
        }

        if (audioFile.isEmpty()) {
            model.addAttribute("error", "Please upload an audio file");
            model.addAttribute("user", currentArtist);
            return "song-upload-form";
        }

        if (previewClip.isEmpty()) {
            model.addAttribute("error", "Please upload a preview clip");
            model.addAttribute("user", currentArtist);
            return "song-upload-form";
        }

        if (songImage.isEmpty()) {
            model.addAttribute("error", "Please upload a song image");
            model.addAttribute("user", currentArtist);
            return "song-upload-form";
        }

        // Validate file types
//        if (!isValidAudioFile(audioFile)) {
//            model.addAttribute("error", "Please upload a valid audio file (MP3, WAV, FLAC, AAC)");
//            model.addAttribute("user", currentArtist);
//            return "song-upload-form";
//        }
//
//        if (!isValidAudioFile(previewClip)) {
//            model.addAttribute("error", "Please upload a valid preview clip (MP3, WAV, FLAC, AAC)");
//            model.addAttribute("user", currentArtist);
//            return "song-upload-form";
//        }

//        if (!isValidImageFile(songImage)) {
//            model.addAttribute("error", "Please upload a valid image file (JPG, JPEG, PNG, GIF)");
//            model.addAttribute("user", currentArtist);
//            return "song-upload-form";
//        }

        try {
            // save files and get paths
            String audioFilePath = songService.saveFile(audioFile);
            String previewClipPath = songService.saveFile(previewClip);
            String songImagePath = songService.saveFile(songImage);

            // create and save song
            songService.createSong(name, genre, price, currentArtist, audioFilePath, previewClipPath, songImagePath);

            return "redirect:/artist/songs?success=Song uploaded successfully";

        } catch (IOException e) {
            model.addAttribute("error", "File upload error: " + e.getMessage());
            model.addAttribute("user", currentArtist);
            return "song-upload-form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", currentArtist);
            return "song-upload-form";
        }
    }

    // Helper method to validate audio files
    private boolean isValidAudioFile(MultipartFile file) {
        if (file.isEmpty()) return false;

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        return (contentType != null && contentType.startsWith("audio/")) ||
                (originalFilename != null &&
                        (originalFilename.toLowerCase().endsWith(".mp3") ||
                                originalFilename.toLowerCase().endsWith(".wav") ||
                                originalFilename.toLowerCase().endsWith(".flac") ||
                                originalFilename.toLowerCase().endsWith(".aac") ||
                                originalFilename.toLowerCase().endsWith(".m4a")));
    }

    // Helper method to validate image files
    private boolean isValidImageFile(MultipartFile file) {
        if (file.isEmpty()) return false;

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        return (contentType != null && contentType.startsWith("image/")) ||
                (originalFilename != null &&
                        (originalFilename.toLowerCase().endsWith(".jpg") ||
                                originalFilename.toLowerCase().endsWith(".jpeg") ||
                                originalFilename.toLowerCase().endsWith(".png") ||
                                originalFilename.toLowerCase().endsWith(".gif") ||
                                originalFilename.toLowerCase().endsWith(".bmp")));
    }

    // List all songs for current artist
    @GetMapping
    public String listArtistSongs(Model model,
                                  @RequestParam(required = false) String success,
                                  @RequestParam(required = false) String error) {

        Artist currentArtist = getCurrentArtist();
        if (currentArtist == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        List<Song> songs = songService.getSongsByArtist(currentArtist);

        model.addAttribute("songs", songs);
        model.addAttribute("user", currentArtist);

        if (success != null) model.addAttribute("success", success);
        if (error != null) model.addAttribute("error", error);

        return "artist-songs";
    }

    // View song details
    @GetMapping("/{id}")
    public String viewSong(@PathVariable Long id, Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/users/login";
        }

        Optional<Song> song = songService.getSongById(id);
        if (song.isEmpty()) {
            return "redirect:/artist/songs?error=Song not found";
        }

        model.addAttribute("song", song.get());
        model.addAttribute("user", currentUser);
        return "song-details";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Artist currentArtist = getCurrentArtist();
        if (currentArtist == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        Optional<Song> song = songService.getSongById(id);
        if (song.isEmpty()) {
            return "redirect:/artist/songs?error=Song not found";
        }

        // Check ownership
        if (!song.get().getArtist().getUserId().equals(currentArtist.getUserId())) {
            return "redirect:/artist/songs?error=Unauthorized access";
        }

        model.addAttribute("song", song.get());
        model.addAttribute("user", currentArtist);
        return "song-edit-form";
    }

    // Handle song update
    @PostMapping("/edit/{id}")
    public String updateSong(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam String genre,
                             @RequestParam Double price,
                             Model model) {

        Artist currentArtist = getCurrentArtist();
        if (currentArtist == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        // Validation for edit form
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("error", "Please enter a song name");
            return "redirect:/artist/songs/edit/" + id;
        }

        if (genre == null || genre.trim().isEmpty()) {
            model.addAttribute("error", "Please enter a genre");
            return "redirect:/artist/songs/edit/" + id;
        }

        if (price == null || price < 0) {
            model.addAttribute("error", "Please enter a valid price");
            return "redirect:/artist/songs/edit/" + id;
        }

        try {
            // Check ownership
            if (!songService.isArtistOwner(id, currentArtist.getUserId())) {
                return "redirect:/artist/songs?error=Unauthorized access";
            }

            songService.updateSong(id, name, genre, price);
            return "redirect:/artist/songs?success=Song updated successfully";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/artist/songs/edit/" + id;
        }
    }

    // to delete song
    @GetMapping("/delete/{id}")
    public String deleteSong(@PathVariable Long id) {
        Artist currentArtist = getCurrentArtist();
        if (currentArtist == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        // Check ownership before deletion
        if (songService.isArtistOwner(id, currentArtist.getUserId())) {
            songService.deleteSong(id);
            return "redirect:/artist/songs?success=Song deleted successfully";
        } else {
            return "redirect:/artist/songs?error=Unauthorized access";
        }
    }
}