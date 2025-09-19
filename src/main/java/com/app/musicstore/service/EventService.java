package com.app.musicstore.service;

import com.app.musicstore.model.entity.Event;
import com.app.musicstore.model.entity.User;
import com.app.musicstore.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public Event createEvent(Event event, User artist) {
        if (!artist.getRole().equals("ARTIST")) {
            throw new RuntimeException("Only artists can create events");
        }
        event.setArtist(artist);
        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}
