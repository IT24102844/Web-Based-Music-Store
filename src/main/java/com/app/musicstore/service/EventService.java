package com.app.musicstore.service;

import com.app.musicstore.model.Event;
import com.app.musicstore.model.User;
import com.app.musicstore.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public Event createEvent(Event event, User artist) {
        // Simple artist validation
        if (artist == null || !artist.isArtist()) {
            throw new RuntimeException("Only artists can create events");
        }

        event.setArtistId(artist.getId());
        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getArtistEvents(User artist) {
        if (artist == null || !artist.isArtist()) {
            return List.of();
        }
        return eventRepository.findByArtistId(artist.getId());
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    public Event updateEvent(Event event) {
        if (event.getId() == null) {
            throw new RuntimeException("Event ID is required for update");
        }
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
        }
    }
}