package com.app.musicstore.repository;

import com.app.musicstore.model.Song;
import com.app.musicstore.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // Find all songs by an artist
    List<Song> findByArtist(Artist artist);

    // Find songs by artist ID
    @Query("SELECT s FROM Song s WHERE s.artist.userId = :artistId")
    List<Song> findByArtistId(@Param("artistId") Long artistId);

    // Find songs by genre
    List<Song> findByGenre(String genre);

    // Find songs by name
    List<Song> findByNameContainingIgnoreCase(String name);

    // Find all songs ordered by upload date (newest first)
    List<Song> findAllByOrderByUploadDateDesc();

    // Check if song exists by name and artist (for duplicate prevention)
    boolean existsByNameAndArtist(String name, Artist artist);

    @Query("SELECT DISTINCT s.genre FROM Song s")
    List<String> findDistinctGenres();
}