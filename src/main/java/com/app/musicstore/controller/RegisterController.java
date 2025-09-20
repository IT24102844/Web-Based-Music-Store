package com.app.musicstore.controller;

import com.app.musicstore.model.User;
import com.app.musicstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    // FIXED: Route matches your template
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // FIXED: Matches your template fields (name, email, password, role)
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model, HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // Validate input
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                model.addAttribute("error", "Email is required");
                return "register";
            }

            if (user.getPassword() == null || user.getPassword().length() < 6) {
                model.addAttribute("error", "Password must be at least 6 characters");
                return "register";
            }

            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match");
                return "register";
            }

            // FIXED: Check if email already exists
            if (userService.findByEmail(user.getEmail()).isPresent()) {
                model.addAttribute("error", "Email already registered");
                return "register";
            }

            // FIXED: Set default values
            user.setStatus("ACTIVE");
            user.setCreatedAt(java.time.LocalDateTime.now());
            user.setUpdatedAt(java.time.LocalDateTime.now());

            // FIXED: Set role from form (your template has role field)
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                user.setRole("BUYER"); // Default role
            }

            // Save user
            User savedUser = userService.saveUser(user);

            // Auto-login after registration
            session.setAttribute("currentUser", savedUser);
            session.setAttribute("userEmail", savedUser.getEmail());

            // Redirect based on role
            if ("ARTIST".equals(savedUser.getRole())) {
                redirectAttributes.addFlashAttribute("success", "Registration successful! Welcome, artist!");
                return "redirect:/artist/events";
            } else {
                redirectAttributes.addFlashAttribute("success", "Registration successful! Welcome to Music Store!");
                return "redirect:/events";
            }

        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }
}