package com.app.musicstore.controller;

import com.app.musicstore.model.User;
import com.app.musicstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("user", sessionUser);
        return "dashboard"; // this should point to dashboard.html
    }


    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // templates/register.html
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/users/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // templates/login.html
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {
        Optional<User> user = userService.login(email, password);
        if (user.isPresent()) {
            session.setAttribute("loggedInUser", user.get()); // save user in session
            model.addAttribute("user", user.get());
            return "dashboard";
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }

    @GetMapping("/edit-profile")
    public String editProfileForm(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/users/login";
        }

        // Fetch latest user details from DB
        User user = userService.getUserById(sessionUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "edit-profile"; // points to edit-profile.html
    }


    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User user, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/users/login";
        }

        userService.updateUser(sessionUser.getUserId(), user);

        // refresh session with updated user
        session.setAttribute("loggedInUser",
                userService.getUserById(sessionUser.getUserId()).get());

        return "redirect:/users/dashboard";
    }


    @GetMapping("/logout")
    public String logout() {
        return "redirect:/users/login";
    }

}
