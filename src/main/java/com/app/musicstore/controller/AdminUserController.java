package com.app.musicstore.controller;

import com.app.musicstore.model.Role;
import com.app.musicstore.model.User;
import com.app.musicstore.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // ✅ List all users
    @GetMapping
    public String listUsers(Model model, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }
        model.addAttribute("users", userService.getAllUsers());
        return "admin-user-list";
    }

    // ✅ Edit user form
    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "admin-edit-user";
    }

    // ✅ Update user
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
        userService.updateUser(id, user);
        return "redirect:/admin/users";
    }

    // ✅ Delete user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    // ✅ Change role
    @PostMapping("/role/{id}")
    public String changeRole(@PathVariable Long id, @RequestParam String role) {
        userService.changeUserRole(id, role.toUpperCase());
        return "redirect:/admin/users";
    }

    // ✅ Change status
    @PostMapping("/status/{id}")
    public String changeStatus(@PathVariable Long id, @RequestParam String status) {
        userService.changeUserStatus(id, status.toUpperCase());
        return "redirect:/admin/users";
    }
}

