package com.app.musicstore.repository;

import com.app.musicstore.model.Artist;
import com.app.musicstore.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find all events by Artist object
    List<Event> findByArtist(Artist artist);

    // Find all events by Artist ordered by date descending
    List<Event> findByArtistOrderByDateDesc(Artist artist);
}
