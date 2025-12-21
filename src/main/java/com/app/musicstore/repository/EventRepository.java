package com.app.musicstore.repository;

import com.app.musicstore.model.Artist;
import com.app.musicstore.model.Event;
import com.app.musicstore.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find all events by Artist object
    List<Event> findByArtist(Artist artist);

    // Find all events by Artist ordered by date descending
    List<Event> findByArtistOrderByDateDesc(Artist artist);

    // ✅ Find events by status
    List<Event> findByStatus(EventStatus status);

    // ✅ Find events by status ordered by date
    List<Event> findByStatusOrderByDateDesc(EventStatus status);

    // ✅ Find events by artist and status
    List<Event> findByArtistAndStatus(Artist artist, EventStatus status);

    // ✅ Find events by artist and status ordered by date
    List<Event> findByArtistAndStatusOrderByDateDesc(Artist artist, EventStatus status);
}