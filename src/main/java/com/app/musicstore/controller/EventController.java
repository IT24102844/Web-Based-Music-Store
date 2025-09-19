package com.app.musicstore.controller;

import com.app.musicstore.model.entity.Event;
import com.app.musicstore.model.entity.User;
import com.app.musicstore.repository.EventRepository;
import com.app.musicstore.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class EventController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // Upload folder (relative to project root)
    private static final String UPLOAD_DIR = "uploads/";

    public EventController(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // Show form to create new event
    @GetMapping("/artist/event/new")
    public String newEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "event_form";
    }

    // Save new event with image
    @PostMapping("/artist/event/save")
    public String saveEvent(@ModelAttribute Event event,
                            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        User artist = userRepository.findAll()
                .stream()
                .filter(u -> "ARTIST".equals(u.getRole()))
                .findFirst()
                .orElse(null);

        event.setArtist(artist);

        // Save image if uploaded
        /*if (!imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath.toFile());
            event.setImagePath("/" + UPLOAD_DIR + fileName);  // saved path for web access
        }*/

        eventRepository.save(event);
        return "redirect:/artist/events";
    }

    // List all events
    @GetMapping("/events")
    public String listEvents(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        return "event_list";
    }

    @GetMapping("/artist/events")
    public String artistEvents(Model model) {
        User artist = userRepository.findAll()
                .stream()
                .filter(u -> "ARTIST".equals(u.getRole()))
                .findFirst()
                .orElse(null);

        model.addAttribute("events", eventRepository.findAll()
                .stream()
                .filter(e -> e.getArtist() != null && e.getArtist().equals(artist))
                .toList());

        model.addAttribute("artist", artist);
        return "event_home_page";
    }

    @GetMapping("/artist/event/edit/{id}")
    public String editEvent(@PathVariable Long id, Model model) {
        Event event = eventRepository.findById(id).orElseThrow();
        model.addAttribute("event", event);
        return "event_form";
    }

    @PostMapping("/artist/event/update/{id}")
    public String updateEvent(@PathVariable Long id,
                              @ModelAttribute Event event)
                             // @RequestParam("imageFile") MultipartFile imageFile throws IOException 
                              {

        Event existingEvent = eventRepository.findById(id).orElseThrow();
        existingEvent.setTitle(event.getTitle());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setDate(event.getDate());
        existingEvent.setVenue(event.getVenue());
        existingEvent.setTicketPrice(event.getTicketPrice());

        
        eventRepository.save(existingEvent);
        return "redirect:/events";

       /*  // Update image if a new file is uploaded
        if (!imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath.toFile());
            existingEvent.setImagePath("/" + UPLOAD_DIR + fileName);
        }*/

       
    }

    @GetMapping("/artist/event/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventRepository.deleteById(id);
        return "redirect:/events";
    }
}
