package com.app.musicstore.controller;

import com.app.musicstore.model.*;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.ArtistService;
import com.app.musicstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public UserController(UserService userService,
                          BCryptPasswordEncoder passwordEncoder,
                          ArtistService artistService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.artistService = artistService;
    }

    /* ===================== REGISTER ===================== */

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                               @RequestParam Map<String, String> allParams) {
        try {
            System.out.println("=== REGISTRATION DEBUG ===");
            System.out.println("Name: " + user.getName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role: " + user.getRole());
            System.out.println("Params: " + allParams);

            userService.registerUserWithDetails(user, allParams);
            return "redirect:/users/login?success=Registration successful! Please login.";
        } catch (IllegalArgumentException e) {
            return "redirect:/users/register?error=" + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/users/register?error=Registration failed: " + e.getMessage();
        }
    }

    /* ===================== LOGIN ===================== */

    @GetMapping("/login")
    public String showLoginForm(@RequestParam Optional<String> error,
                                @RequestParam Optional<String> success,
                                Model model) {
        error.ifPresent(e -> model.addAttribute("error", e));
        success.ifPresent(s -> model.addAttribute("success", s));
        return "login";
    }

    /* ===================== PROFILE EDIT (GENERAL USERS) ===================== */

    @GetMapping("/edit-profile")
    public String editProfileForm(Model model) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return "redirect:/users/login";
        }

        var user = userService.getUserById(authenticatedUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("currentUser", authenticatedUser);
        return "edit-profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String name,
                                @RequestParam(required = false) String phoneNo,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String password,
                                HttpSession session) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return "redirect:/users/login";
        }

        try {
            // Get current user from database - work with the existing object
            User currentUser = userService.getUserById(authenticatedUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update only the fields that should change
            currentUser.setName(name);
            currentUser.setPhoneNo(phoneNo);
            currentUser.setAddress(address);

            // Handle password - only update if provided and not empty
            if (password != null && !password.trim().isEmpty()) {
                if (password.length() < 6) {
                    session.setAttribute("error", "Password must be at least 6 characters long");
                    return "redirect:/users/edit-profile";
                }
                currentUser.setPassword(passwordEncoder.encode(password));
            }
            // If password is empty, it keeps the current encoded password

            // Use the service to update the existing user
            User updatedUser = userService.updateUser(authenticatedUser.getUserId(), currentUser);

            // Refresh authentication context
            refreshAuthenticationContext(updatedUser);

            // Update session with latest user data
            session.setAttribute("loggedInUser", updatedUser);

            return switch (updatedUser.getRole()) {
                case ADMIN -> "redirect:/dashboard/admin?success=Profile updated successfully";
                case ARTIST -> "redirect:/dashboard/artist?success=Profile updated successfully";
                case ITEM_SELLER -> "redirect:/dashboard/item-seller?success=Profile updated successfully";
                case COURSE_SELLER -> "redirect:/dashboard/course-seller?success=Profile updated successfully";
                case CUSTOMER -> "redirect:/dashboard/customer?success=Profile updated successfully";
            };
        } catch (Exception e) {
            session.setAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/users/edit-profile";
        }
    }

    /* ===================== ARTIST PROFILE ===================== */

    @GetMapping("/edit-artist-profile")
    public String editArtistProfileForm(Model model) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return "redirect:/users/login";
        }
        if (authenticatedUser.getRole() != Role.ARTIST) {
            return "redirect:/users/edit-profile";
        }

        var user = userService.getUserById(authenticatedUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);

        artistService.findByUserId(authenticatedUser.getUserId()).ifPresentOrElse(artist -> {
            model.addAttribute("stageName", artist.getStageName());
            model.addAttribute("genre", artist.getGenre());
        }, () -> {
            model.addAttribute("stageName", "");
            model.addAttribute("genre", "");
        });

        model.addAttribute("currentUser", authenticatedUser);
        return "artist-edit-profile";
    }

    @PostMapping("/update-artist-profile")
    public String updateArtistProfile(@RequestParam Map<String, String> allParams,
                                      HttpSession session,
                                      Model model) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) return "redirect:/users/login";

        try {
            Long userId = authenticatedUser.getUserId();
            String name = allParams.get("name");
            String phoneNo = allParams.get("phoneNo");
            String address = allParams.get("address");
            String password = allParams.get("password");
            String stageName = allParams.get("stageName");
            String genre = allParams.get("genre");

            if (stageName == null || stageName.isBlank() || genre == null || genre.isBlank()) {
                model.addAttribute("error", "Stage name and genre are required");
                return editArtistProfileForm(model);
            }

            // Update user info
            User currentUser = userService.getUserById(userId).orElseThrow();
            currentUser.setName(name);
            currentUser.setPhoneNo(phoneNo);
            currentUser.setAddress(address);

            if (password != null && !password.trim().isEmpty()) {
                if (password.length() < 6) {
                    model.addAttribute("error", "Password must be at least 6 characters");
                    return editArtistProfileForm(model);
                }
                currentUser.setPassword(passwordEncoder.encode(password));
            }

            User updatedUser = userService.updateUser(userId, currentUser);

            // Update or create artist info
            artistService.findByUserId(userId).ifPresentOrElse(
                    a -> artistService.updateArtistDetails(userId, stageName.trim(), genre.trim()),
                    () -> {
                        Artist newArtist = new Artist();
                        newArtist.setUserId(userId);
                        newArtist.setStageName(stageName.trim());
                        newArtist.setGenre(genre.trim());
                        artistService.save(newArtist);
                    }
            );

            // ✅ Refresh authentication context for artist as well
            refreshAuthenticationContext(updatedUser);

            session.setAttribute("loggedInUser", updatedUser);
            return "redirect:/dashboard/artist?success=Artist profile updated successfully";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return editArtistProfileForm(model);
        }
    }

    /* ===================== COMPLETE PROFILE ===================== */

    @GetMapping("/complete-profile")
    public String showCompleteProfileForm(HttpSession session, Model model) {
        var user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        return switch (user.getRole()) {
            case ARTIST -> {
                model.addAttribute("artist", new Artist());
                yield "complete-artist-profile";
            }
            case CUSTOMER -> {
                model.addAttribute("customer", new Customer());
                yield "complete-customer-profile";
            }
            case COURSE_SELLER -> {
                model.addAttribute("courseSeller", new CourseSeller());
                yield "complete-course-seller-profile";
            }
            case ITEM_SELLER -> {
                model.addAttribute("instrumentSeller", new InstrumentSeller());
                yield "complete-instrument-seller-profile";
            }
            default -> "redirect:/dashboard";
        };
    }

    /* ===================== PASSWORD RESET ===================== */

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
            return "redirect:/users/login?success=Password reset successfully!";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }

    /* ===================== DEBUG & UTIL ===================== */

    @GetMapping("/check-artist-profile")
    @ResponseBody
    public String checkArtistProfile() {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) return "No authenticated user";

        return artistService.findByUserId(authenticatedUser.getUserId())
                .map(artist -> "Artist found - Stage: " + artist.getStageName() + ", Genre: " + artist.getGenre())
                .orElse("No artist profile found for user " + authenticatedUser.getEmail());
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        return null;
    }

    // ✅ NEW METHOD: Refresh authentication context after profile updates
    private void refreshAuthenticationContext(User updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            // Update the user in CustomUserDetails
            userDetails.setUser(updatedUser);

            // Create new authentication token with updated details
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    authentication.getCredentials(),
                    authentication.getAuthorities()
            );

            // Set the updated authentication in security context
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            System.out.println("✅ Authentication context refreshed for user: " + updatedUser.getEmail());
        }
    }
}