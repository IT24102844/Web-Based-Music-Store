package com.app.musicstore.controller;

import com.app.musicstore.model.entity.Event;
import com.app.musicstore.model.entity.User;
import com.app.musicstore.service.EventService;
import com.app.musicstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    // Artist Dashboard
    @GetMapping("/dashboard")
    public String artistDashboard(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ARTIST".equals(currentUser.getRole())) {
            return "redirect:/login?error=artistRequired";
        }

        List<Event> userEvents = eventService.getArtistEvents(currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("events", userEvents);
        return "artist-dashboard";
    }

    // User list (admin only)
    @GetMapping("/list")
    public String listUsers(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/events?error=adminRequired";
        }

        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        return "home";
    }

    // Profile view
    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?error=loginRequired";
        }

        model.addAttribute("user", currentUser);
        return "user_profile";
    }

    // Profile update
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User user,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?error=loginRequired";
        }

        try {
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                if (!user.getEmail().equals(currentUser.getEmail()) &&
                        userService.findByEmail(user.getEmail()).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Email already in use");
                    return "redirect:/users/profile";
                }
                currentUser.setEmail(user.getEmail());
            }

            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                currentUser.setPassword(user.getPassword());
            }

            currentUser.setUpdatedAt(java.time.LocalDateTime.now());
            User updatedUser = userService.saveUser(currentUser);

            // FIXED: Update session with updated user
            session.setAttribute("currentUser", updatedUser);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/users/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/users/profile";
        }
    }
}