package com.app.musicstore.controller;

import com.app.musicstore.model.*;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.UserService;
import com.app.musicstore.service.ArtistService;
import com.app.musicstore.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ArtistService artistService;
    private final UserRepository userRepository;

    public UserController(UserService userService,
                          BCryptPasswordEncoder passwordEncoder,
                          ArtistService artistService,
                          UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.artistService = artistService;
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                               @RequestParam Map<String, String> allParams) {
        try {
            userService.registerUserWithDetails(user, allParams);
            return "redirect:/users/login?success=Registration successful! Please login.";
        } catch (IllegalArgumentException e) {
            return "redirect:/users/register?error=" + e.getMessage();
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam Optional<String> error,
                                @RequestParam Optional<String> success,
                                Model model) {
        error.ifPresent(e -> model.addAttribute("error", e));
        success.ifPresent(s -> model.addAttribute("success", s));
        return "login";
    }

    // Regular edit profile for all users
    @GetMapping("/edit-profile")
    public String editProfileForm(Model model) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return "redirect:/users/login";
        }

        // If user is artist, redirect to artist edit profile
        if (authenticatedUser.getRole() == Role.ARTIST) {
            return "redirect:/users/edit-artist-profile";
        }

        var user = userService.getUserById(authenticatedUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("currentUser", authenticatedUser);
        return "edit-profile";
    }

    // Regular profile update for users
    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User user,
                                HttpSession session,
                                @RequestParam Map<String, String> allParams,
                                Model model) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return "redirect:/users/login";
        }

        try {
            System.out.println("🔄 Starting profile update for user: " + authenticatedUser.getEmail());

            // Get current user from database
            User currentUser = userService.getUserById(authenticatedUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Handle password - if password is empty or null, keep the current one
            String newPassword = user.getPassword();
            if (newPassword == null || newPassword.trim().isEmpty()) {
                // Keep current password
                user.setPassword(currentUser.getPassword());
            } else {
                // Validate password length
                if (newPassword.length() < 6) {
                    model.addAttribute("error", "Password must be at least 6 characters long");
                    return editProfileForm(model);
                }
                // New password provided - encode it
                user.setPassword(passwordEncoder.encode(newPassword));
            }

            // Preserve other fields that shouldn't be changed
            user.setEmail(currentUser.getEmail());
            user.setRole(currentUser.getRole());
            user.setUserId(currentUser.getUserId());
            user.setCreatedAt(currentUser.getCreatedAt());
            user.setStatus(currentUser.getStatus());

            // Update User table
            System.out.println("💾 Updating User table...");
            User updatedUser = userService.updateUser(authenticatedUser.getUserId(), user);
            System.out.println("✅ User table updated: " + updatedUser.getName());

            // Update session with latest user data
            session.setAttribute("loggedInUser", updatedUser);

            System.out.println("🎉 Profile update completed successfully!");

            // Redirect based on role
            return switch (updatedUser.getRole()) {
                case ADMIN -> "redirect:/dashboard/admin?success=Profile updated successfully";
                case ARTIST -> "redirect:/dashboard/artist?success=Profile updated successfully";
                case ITEM_SELLER -> "redirect:/dashboard/item-seller?success=Profile updated successfully";
                case COURSE_SELLER -> "redirect:/dashboard/course-seller?success=Profile updated successfully";
                case CUSTOMER -> "redirect:/dashboard/customer?success=Profile updated successfully";
            };

        } catch (Exception e) {
            System.out.println("❌ Error updating profile: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error updating profile: " + e.getMessage());
            return editProfileForm(model);
        }
    }

    // Artist-specific edit profile
    @GetMapping("/edit-artist-profile")
    public String editArtistProfileForm(Model model) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return "redirect:/users/login";
        }

        // Check if user is actually an artist
        if (authenticatedUser.getRole() != Role.ARTIST) {
            return "redirect:/users/edit-profile";
        }

        var user = userService.getUserById(authenticatedUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);

        // Load artist-specific fields
        Optional<Artist> artistOpt = artistService.findByUserId(authenticatedUser.getUserId());
        if (artistOpt.isPresent()) {
            Artist artist = artistOpt.get();
            model.addAttribute("stageName", artist.getStageName());
            model.addAttribute("genre", artist.getGenre());
            System.out.println("✅ Loaded artist data for edit - Stage: " + artist.getStageName() + ", Genre: " + artist.getGenre());
        } else {
            // If artist profile doesn't exist yet, set empty values
            model.addAttribute("stageName", "");
            model.addAttribute("genre", "");
            System.out.println("⚠️ No artist profile found for user: " + authenticatedUser.getUserId());
        }

        model.addAttribute("currentUser", authenticatedUser);
        return "artist-edit-profile";
    }

    // Artist-specific profile update
    @PostMapping("/update-artist-profile")
    public String updateArtistProfile(@RequestParam Map<String, String> allParams,
                                      HttpSession session,
                                      Model model) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return "redirect:/users/login";
        }

        try {
            System.out.println("🔄 Starting artist profile update for user: " + authenticatedUser.getEmail());

            // Get parameters
            Long userId = Long.parseLong(allParams.get("userId"));
            String name = allParams.get("name");
            String phoneNo = allParams.get("phoneNo");
            String address = allParams.get("address");
            String password = allParams.get("password");
            String stageName = allParams.get("stageName");
            String genre = allParams.get("genre");

            System.out.println("🎤 Stage Name: " + stageName);
            System.out.println("🎵 Genre: " + genre);
            System.out.println("👤 Name: " + name);

            // Validate required fields
            if (stageName == null || stageName.trim().isEmpty()) {
                model.addAttribute("error", "Stage name is required");
                return editArtistProfileForm(model);
            }
            if (genre == null || genre.trim().isEmpty()) {
                model.addAttribute("error", "Genre is required");
                return editArtistProfileForm(model);
            }

            // Get current user from database
            User currentUser = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create updated user object
            User updatedUser = new User();
            updatedUser.setUserId(userId);
            updatedUser.setName(name);
            updatedUser.setEmail(currentUser.getEmail());
            updatedUser.setPhoneNo(phoneNo);
            updatedUser.setAddress(address);
            updatedUser.setRole(currentUser.getRole());
            updatedUser.setStatus(currentUser.getStatus());
            updatedUser.setCreatedAt(currentUser.getCreatedAt());

            // Handle password
            if (password == null || password.trim().isEmpty()) {
                updatedUser.setPassword(currentUser.getPassword());
            } else {
                if (password.length() < 6) {
                    model.addAttribute("error", "Password must be at least 6 characters long");
                    return editArtistProfileForm(model);
                }
                updatedUser.setPassword(passwordEncoder.encode(password));
            }

            // Update User table
            System.out.println("💾 Updating User table...");
            User savedUser = userService.updateUser(userId, updatedUser);
            System.out.println("✅ User table updated: " + savedUser.getName());

            // Update Artist table
            System.out.println("🎭 Updating Artist table...");

            // Check if artist profile exists
            Optional<Artist> existingArtist = artistService.findByUserId(userId);
            if (existingArtist.isPresent()) {
                System.out.println("📝 Found existing artist profile, updating...");
                Artist updatedArtist = artistService.updateArtistDetails(userId, stageName.trim(), genre.trim());
                System.out.println("✅ Artist table updated - Stage: " + updatedArtist.getStageName() + ", Genre: " + updatedArtist.getGenre());
            } else {
                System.out.println("🆕 Creating new artist profile...");
                // Create new artist profile
                Artist newArtist = new Artist();
                // Copy user properties to artist
                newArtist.setUserId(savedUser.getUserId());
                newArtist.setName(savedUser.getName());
                newArtist.setEmail(savedUser.getEmail());
                newArtist.setPassword(savedUser.getPassword());
                newArtist.setPhoneNo(savedUser.getPhoneNo());
                newArtist.setAddress(savedUser.getAddress());
                newArtist.setRole(savedUser.getRole());
                newArtist.setStatus(savedUser.getStatus());
                newArtist.setCreatedAt(savedUser.getCreatedAt());
                newArtist.setUpdatedAt(savedUser.getUpdatedAt());

                // Set artist-specific fields
                newArtist.setStageName(stageName.trim());
                newArtist.setGenre(genre.trim());

                Artist savedArtist = artistService.save(newArtist);
                System.out.println("✅ New artist profile created - Stage: " + savedArtist.getStageName() + ", Genre: " + savedArtist.getGenre());
            }

            // Update session
            session.setAttribute("loggedInUser", savedUser);

            System.out.println("🎉 Artist profile update completed successfully!");
            return "redirect:/dashboard/artist?success=Artist profile updated successfully";

        } catch (Exception e) {
            System.out.println("❌ Error updating artist profile: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error updating profile: " + e.getMessage());
            return editArtistProfileForm(model);
        }
    }

    @GetMapping("/complete-profile")
    public String showCompleteProfileForm(HttpSession session, Model model) {
        var user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/users/login";
        }

        return switch (user.getRole()) {
            case ARTIST -> {
                model.addAttribute("artist", new Artist());
                yield "complete-artist-profile";
            }
            case CUSTOMER -> {
                model.addAttribute("customer", new Customer());
                yield "complete-customer-profile";
            }
            default -> "redirect:/dashboard";
        };
    }

    // Debug endpoint to check artist profile
    @GetMapping("/check-artist-profile")
    @ResponseBody
    public String checkArtistProfile() {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return "No authenticated user";
        }

        Optional<Artist> artistOpt = artistService.findByUserId(authenticatedUser.getUserId());
        if (artistOpt.isPresent()) {
            Artist artist = artistOpt.get();
            return "Artist profile found - ID: " + artist.getUserId() +
                    ", Stage Name: " + artist.getStageName() +
                    ", Genre: " + artist.getGenre();
        } else {
            return "No artist profile found for user: " + authenticatedUser.getEmail() +
                    " (User ID: " + authenticatedUser.getUserId() + ")";
        }
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

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        @RequestParam String newPassword,
                                        Model model) {
        try {
            userService.resetPassword(email, newPassword);
            return "redirect:/users/login?success=Password reset successfully! Please login with your new password.";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }
}