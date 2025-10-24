package com.app.musicstore.service;

import com.app.musicstore.model.Song;
import com.app.musicstore.model.Artist;
import com.app.musicstore.repository.SongRepository;
import com.app.musicstore.repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SongService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;

    // @Value("${file.upload-dir:C:/Users/acer/Desktop/SE/SE_project/Web-Based-Music-Store/uploads}")
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    public SongService(SongRepository songRepository, ArtistRepository artistRepository) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
    }

    @PostConstruct
    public void init() {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    public Song saveSong(Song song) {
        return songRepository.save(song);
    }

    public List<Song> getAllSongs() {
        return songRepository.findAllByOrderByUploadDateDesc();
    }

    public List<Song> getSongsByArtist(Artist artist) {
        return songRepository.findByArtist(artist);
    }

    public List<Song> getSongsByArtistId(Long artistId) {
        return songRepository.findByArtistId(artistId);
    }

    public Optional<Song> getSongById(Long id) { // Changed from songId to id
        return songRepository.findById(id);
    }

    public void deleteSong(Long id) {
        if (!songRepository.existsById(id)) {
            throw new RuntimeException("Song not found with id: " + id);
        }
        songRepository.deleteById(id);
    }

    // Enhanced methods for file handling and business logic
    public Song createSong(String name, String genre, Double price, Artist artist,
            String audioFilePath, String previewClipPath, String songImagePath) {

        // Check for duplicate song name for this artist
        if (songRepository.existsByNameAndArtist(name, artist)) {
            throw new IllegalArgumentException("You already have a song with the name: " + name);
        }

        Song song = new Song();
        song.setName(name);
        song.setGenre(genre);
        song.setPrice(price);
        song.setArtist(artist);
        song.setAudioFile(audioFilePath);
        song.setPreviewClip(previewClipPath);
        song.setSongImage(songImagePath);
        song.setUploadDate(LocalDate.now());

        return songRepository.save(song);
    }

    public Song updateSong(Long id, String name, String genre, Double price) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + id));

        song.setName(name);
        song.setGenre(genre);
        song.setPrice(price);

        return songRepository.save(song);
    }

    public List<Song> getSongsByGenre(String genre) {
        return songRepository.findByGenre(genre);
    }

    public List<Song> searchSongs(String query) {
        return songRepository.findByNameContainingIgnoreCase(query);
    }

    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Use the external uploads directory configured at the top of this class
        Path directoryPath = Paths.get(uploadDir, "songs");

        // Create directory if it doesn't exist
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = directoryPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath);

        return "/uploads/songs/" + fileName;
    }

    public boolean isArtistOwner(Long songId, Long artistId) {
        return songRepository.findById(songId)
                .map(song -> song.getArtist().getUserId().equals(artistId))
                .orElse(false);
    }

    public List<String> getAllGenres() {
        return songRepository.findAll().stream()
                .map(Song::getGenre)
                .distinct()
                .toList();
    }

    // Get all songs for customers
    public List<Song> getAllSongsForCustomers() {
        return songRepository.findAllByOrderByUploadDateDesc();
    }
}
