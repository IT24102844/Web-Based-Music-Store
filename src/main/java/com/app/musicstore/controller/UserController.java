package com.app.musicstore.controller;

import com.app.musicstore.model.*;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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

    @GetMapping("/edit-profile")
    public String editProfileForm(HttpSession session, Model model) {
        var sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/users/login";
        }

        var user = userService.getUserById(sessionUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "edit-profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User user,
                                HttpSession session,
                                @RequestParam Map<String, String> allParams) {
        var sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/users/login";
        }

        userService.updateUser(sessionUser.getUserId(), user);

        // Update session with latest user data
        var updatedUser = userService.getUserById(sessionUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found after update"));
        session.setAttribute("loggedInUser", updatedUser);

        return switch (updatedUser.getRole()) {
            case ADMIN -> "redirect:/dashboard/admin?success=Profile updated successfully";
            case ARTIST -> "redirect:/dashboard/artist?success=Profile updated successfully";
            case ITEM_SELLER -> "redirect:/dashboard/item-seller?success=Profile updated successfully";
            case COURSE_SELLER -> "redirect:/dashboard/course-seller?success=Profile updated successfully";
            case CUSTOMER -> "redirect:/dashboard/customer?success=Profile updated successfully";
        };
    }

    @GetMapping("/complete-profile")
    public String showCompleteProfileForm(HttpSession session, Model model) {
        var user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/users/login";
        }

        // Use switch expression
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
            default -> "redirect:/dashboard";
        };
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
}