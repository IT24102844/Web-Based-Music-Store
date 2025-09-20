package com.app.musicstore.controller;

import com.app.musicstore.model.Event;
import com.app.musicstore.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private EventService eventService;

    @GetMapping("/")
    public String landingPage(Model model, HttpSession session) {
        // Show recent events on landing page
        model.addAttribute("events", eventService.getAllEvents());
        return "landing";
    }
}