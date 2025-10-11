package com.app.musicstore.service;

import com.app.musicstore.model.Artist;
import com.app.musicstore.model.Event;
import com.app.musicstore.model.EventStatus;
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
        event.setStatus(EventStatus.PENDING); // Set to pending by default
        return eventRepository.save(event);
    }

    /**
     * Get all APPROVED events for public view.
     */
    public List<Event> getAllEvents() {
        return eventRepository.findByStatusOrderByDateDesc(EventStatus.APPROVED);
    }

    /**
     * Get all events including pending/rejected - for admin use
     */
    public List<Event> getAllEventsWithAllStatus() {
        return eventRepository.findAll();
    }

    /**
     * Get all APPROVED events for public view.
     */
    public List<Event> getApprovedEvents() {
        return eventRepository.findByStatusOrderByDateDesc(EventStatus.APPROVED);
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
     * Get events for a specific artist by status
     */
    public List<Event> getArtistEventsByStatus(Artist artist, EventStatus status) {
        if (artist == null) {
            return List.of();
        }
        return eventRepository.findByArtistAndStatusOrderByDateDesc(artist, status);
    }

    /**
     * Get pending events for admin review.
     */
    public List<Event> getPendingEvents() {
        return eventRepository.findByStatus(EventStatus.PENDING);
    }

    /**
     * Get events by status
     */
    public List<Event> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatus(status);
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
     * Approve an event.
     */
    public Event approveEvent(Long eventId) {
        Event event = getEventById(eventId);
        if (event != null) {
            event.setStatus(EventStatus.APPROVED);
            return eventRepository.save(event);
        }
        throw new RuntimeException("Event not found with id: " + eventId);
    }

    /**
     * Reject an event.
     */
    public Event rejectEvent(Long eventId) {
        Event event = getEventById(eventId);
        if (event != null) {
            event.setStatus(EventStatus.REJECTED);
            return eventRepository.save(event);
        }
        throw new RuntimeException("Event not found with id: " + eventId);
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