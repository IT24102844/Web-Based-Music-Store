package com.app.musicstore.service;

import com.app.musicstore.model.Artist;
import com.app.musicstore.model.Event;
import com.app.musicstore.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    /**
     * Create a new event for an artist.
     */
    public Event createEvent(Event event, Artist artist) {
        if (artist == null) {
            throw new RuntimeException("Only artists can create events");
        }
        event.setArtist(artist);
        return eventRepository.save(event);
    }

    /**
     * Get all events.
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Get all events for a specific artist.
     */
    public List<Event> getArtistEvents(Artist artist) {
        if (artist == null) {
            return List.of();
        }
        return eventRepository.findByArtistOrderByDateDesc(artist);
    }

    /**
     * Get an event by its ID.
     */
    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    /**
     * Update an existing event.
     */
    public Event updateEvent(Event event) {
        if (event.getId() == null) {
            throw new RuntimeException("Event ID is required for update");
        }
        return eventRepository.save(event);
    }

    /**
     * Delete an event by ID.
     */
    public void deleteEvent(Long id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
        }
    }
}
