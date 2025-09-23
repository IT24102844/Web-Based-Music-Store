package com.app.musicstore.service;

import com.app.musicstore.model.Artist;
import com.app.musicstore.repository.ArtistRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class ArtistService {

    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public Artist save(Artist artist) {
        return artistRepository.save(artist);
    }

    public Optional<Artist> findByUserId(Long userId) {
        return artistRepository.findByUserId(userId);
    }

    public Artist updateArtistDetails(Long userId, String stageName, String genre) {
        Artist artist = artistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Artist profile not found"));
        artist.setStageName(stageName);
        artist.setGenre(genre);
        return artistRepository.save(artist);
    }
}
