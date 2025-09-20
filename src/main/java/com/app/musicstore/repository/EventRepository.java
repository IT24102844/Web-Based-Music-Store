package com.app.musicstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.musicstore.model.Event;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // FIXED: Add findByArtistId method
    @Query("SELECT e FROM Event e WHERE e.artistId = :artistId")
    List<Event> findByArtistId(@Param("artistId") Long artistId);

    // FIXED: Add findByArtistIdOrderByDateDesc method
    @Query("SELECT e FROM Event e WHERE e.artistId = :artistId ORDER BY e.date DESC")
    List<Event> findByArtistIdOrderByDateDesc(@Param("artistId") Long artistId);

    // Alternative: Native query (if you prefer)
    @Query(value = "SELECT * FROM events WHERE artist = :artistId", nativeQuery = true)
    List<Event> findByArtistNative(@Param("artistId") Long artistId);
}