package com.app.musicstore.controller;

import com.app.musicstore.model.Event;
import com.app.musicstore.model.User;
import com.app.musicstore.service.EventService;
import com.app.musicstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Controller
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    // Public: View all events
    @GetMapping("/events")
    public String listEvents(Model model, HttpSession session) {
        List<Event> events = eventService.getAllEvents();
        User currentUser = getCurrentUserFromSession(session);
        model.addAttribute("events", events);
        model.addAttribute("currentUser", currentUser);
        return "event_list";
    }

    // Artist Only: Create new event
    @GetMapping("/artist/event/new")
    public String newEventForm(Model model, HttpSession session) {
        User currentUser = getCurrentUserFromSession(session);
        if (currentUser == null || !currentUser.isArtist()) {
            return "redirect:/login?error=artistRequired";
        }

        model.addAttribute("event", new Event());
        model.addAttribute("currentUser", currentUser);
        return "event_form";
    }

    // Artist Only: Save new event
    @PostMapping("/artist/event/save")
    public String saveEvent(@ModelAttribute Event event,
            @RequestParam("imageFile") MultipartFile imageFile,
            HttpSession session, Model model) {

        User currentUser = getCurrentUserFromSession(session);
        if (currentUser == null || !currentUser.isArtist()) {
            return "redirect:/login?error=artistRequired";
        }

        try {
            // Set event date if provided
            if (event.getDate() == null) {
                event.setDate(LocalDate.now().plusDays(7)); // Default to next week
            }

            // Handle image upload
            if (!imageFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, imageFile.getBytes());

                event.setImagePath("/uploads/" + fileName);
            }

            // Create event for current artist
            eventService.createEvent(event, currentUser);
            return "redirect:/artist/events?success=eventCreated";

        } catch (IOException e) {
            model.addAttribute("error", "Error uploading image: " + e.getMessage());
            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);
            return "event_form";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create event: " + e.getMessage());
            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);
            return "event_form";
        }
    }

    // Artist Only: My events dashboard
    @GetMapping("/artist/events")
    public String artistEvents(Model model, HttpSession session, @RequestParam(required = false) String error) {
        User currentUser = getCurrentUserFromSession(session);

        if (currentUser == null || !currentUser.isArtist()) {
            return "redirect:/login?error=artistRequired";
        }

        if ("eventCreated".equals(error)) {
            model.addAttribute("success", "Event created successfully!");
        } else if ("noArtist".equals(error)) {
            model.addAttribute("error", "No artist user found.");
        }

        List<Event> artistEvents = eventService.getArtistEvents(currentUser);
        model.addAttribute("events", artistEvents);
        model.addAttribute("currentUser", currentUser);
        return "artist_events";
    }

    // Artist Only: Edit event
    @GetMapping("/artist/event/edit/{id}")
    public String editEvent(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = getCurrentUserFromSession(session);
        if (currentUser == null || !currentUser.isArtist()) {
            return "redirect:/login?error=artistRequired";
        }

        Event event = eventService.getEventById(id);
        if (event == null || !event.getArtistId().equals(currentUser.getId())) {
            return "redirect:/artist/events?error=notFound";
        }

        model.addAttribute("event", event);
        model.addAttribute("currentUser", currentUser);
        return "event_form";
    }

    // Artist Only: Update event
    @PostMapping("/artist/event/update/{id}")
    public String updateEvent(@PathVariable Long id,
            @ModelAttribute Event event,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            HttpSession session, Model model) {

        User currentUser = getCurrentUserFromSession(session);
        if (currentUser == null || !currentUser.isArtist()) {
            return "redirect:/login?error=artistRequired";
        }

        try {
            Event existingEvent = eventService.getEventById(id);
            if (existingEvent == null || !existingEvent.getArtistId().equals(currentUser.getId())) {
                return "redirect:/artist/events?error=unauthorized";
            }

            // Update fields
            existingEvent.setTitle(event.getTitle());
            existingEvent.setDescription(event.getDescription());
            existingEvent.setDate(event.getDate());
            existingEvent.setVenue(event.getVenue());
            existingEvent.setTicketPrice(event.getTicketPrice());

            // Handle image update
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, imageFile.getBytes());

                existingEvent.setImagePath("/uploads/" + fileName);
            }

            eventService.updateEvent(existingEvent);
            return "redirect:/artist/events?success=eventUpdated";

        } catch (Exception e) {
            model.addAttribute("error", "Error updating event: " + e.getMessage());
            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);
            return "event_form";
        }
    }

    // Artist Only: Delete event
    @GetMapping("/artist/event/delete/{id}")
    public String deleteEvent(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUserFromSession(session);
        if (currentUser != null && currentUser.isArtist()) {
            Event event = eventService.getEventById(id);
            if (event != null && event.getArtistId().equals(currentUser.getId())) {
                eventService.deleteEvent(id);
            }
        }
        return "redirect:/artist/events";
    }

    private User getCurrentUserFromSession(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
}