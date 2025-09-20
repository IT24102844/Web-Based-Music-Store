package com.app.musicstore.controller;

import com.app.musicstore.model.User;
import com.app.musicstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String registerUser(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/users/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {
        Optional<User> user = userService.login(email, password);

        if (user.isPresent()) {
            User loggedInUser = user.get();
            session.setAttribute("loggedInUser", loggedInUser);
            model.addAttribute("user", loggedInUser);

            switch (loggedInUser.getRole().name()) {
                case "ADMIN":
                    return "redirect:/dashboard/admin";
                case "ARTIST":
                    return "redirect:/dashboard/artist";
                case "ITEM_SELLER":
                    return "redirect:/dashboard/item-seller";
                case "COURSE_SELLER":
                    return "redirect:/dashboard/course-seller";
                default:
                    return "redirect:/dashboard/customer";
            }
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

        User user = userService.getUserById(sessionUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "edit-profile";
    }


    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User user, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/users/login";
        }

        userService.updateUser(sessionUser.getUserId(), user);

        User updatedUser = userService.getUserById(sessionUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found after update"));
        session.setAttribute("loggedInUser", updatedUser);

        switch (updatedUser.getRole().name()) {
            case "ADMIN":
                return "redirect:/dashboard/admin";
            case "ARTIST":
                return "redirect:/dashboard/artist";
            case "ITEM_SELLER":
                return "redirect:/dashboard/item-seller";
            case "COURSE_SELLER":
                return "redirect:/dashboard/course-seller";
            default:
                return "redirect:/dashboard/customer";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}
