package com.app.musicstore.controller;

import com.app.musicstore.model.Role;
import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // List all users
    @GetMapping
    public String listUsers(Model model) {
        User admin = getAuthenticatedUser();
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);

        long adminCount = users.stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .count();
        model.addAttribute("adminCount", adminCount);

        return "admin-user-list";
    }

    // Edit user form
    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User admin = getAuthenticatedUser();
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "admin-edit-user";
    }

    // Update user
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
        User admin = getAuthenticatedUser();
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        userService.updateUser(id, user);
        return "redirect:/admin/users";
    }

    // Delete user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User admin = getAuthenticatedUser();
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        try {
            // Prevent admin from deleting themselves
            if (admin.getUserId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete your own account");
                return "redirect:/admin/users";
            }

            // Check if this is the last admin
            List<User> allUsers = userService.getAllUsers();
            long adminCount = allUsers.stream()
                    .filter(u -> u.getRole() == Role.ADMIN)
                    .count();

            User userToDelete = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (userToDelete.getRole() == Role.ADMIN && adminCount <= 1) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete the last admin user");
                return "redirect:/admin/users";
            }

            // Perform actual deletion
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // Change role
    @PostMapping("/role/{id}")
    public String changeRole(@PathVariable Long id, @RequestParam String role) {
        User admin = getAuthenticatedUser();
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        userService.changeUserRole(id, role.toUpperCase());
        return "redirect:/admin/users";
    }

    // Change status
    @PostMapping("/status/{id}")
    public String changeStatus(@PathVariable Long id, @RequestParam String status) {
        User admin = getAuthenticatedUser();
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        userService.changeUserStatus(id, status.toUpperCase());
        return "redirect:/admin/users";
    }


    //Get authenticated user from Spring Security context

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