package com.app.musicstore.controller;

import com.app.musicstore.model.User;
import com.app.musicstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    // FIXED: Route matches template
    @GetMapping("/login")
    public String loginForm(Model model, HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            User user = (User) session.getAttribute("currentUser");
            if (user.isArtist()) {
                return "redirect:/artist/events";
            }
            return "redirect:/events";
        }
        model.addAttribute("error", model.containsAttribute("error") ? model.getAttribute("error") : null);
        return "login";
    }

    // FIXED: Route matches template
    @PostMapping("/login")
    public String login(@RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        session.invalidate(); // Clear previous session

        User user = userService.authenticate(email, password);
        if (user != null) {
            session.setAttribute("currentUser", user);
            session.setAttribute("userEmail", user.getEmail());

            if (user.isArtist()) {
                return "redirect:/artist/events";
            } else {
                return "redirect:/events";
            }
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        session.invalidate();
        model.addAttribute("logout", "true");
        return "redirect:/login";
    }

    // Landing page route
    @GetMapping("/")
    public String landing() {
        return "landing";
    }
}