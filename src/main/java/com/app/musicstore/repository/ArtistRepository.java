package com.app.musicstore.repository;

import com.app.musicstore.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findByUserId(Long userId);
    Optional<Artist> findByEmail(String email);
}