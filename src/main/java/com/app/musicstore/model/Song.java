package com.app.musicstore.model;

import jakarta.persistence.*;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Song")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "songId")
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "uploadDate", nullable = true)
    private LocalDate uploadDate;

    @Column(name = "previewClip", length = 255, nullable = true)
    private String previewClip;

    @Column(name = "songImage", length = 255, nullable = true)
    private String songImage;

    @Column(name = "audioFile", length = 255, nullable = false)
    private String audioFile;

    @Column(name = "genre", length = 50, nullable = false)
    private String genre;

    @Column(name = "price", nullable = false)
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artistId", referencedColumnName = "artistId", nullable = false)
    @JsonIgnore
    private Artist artist;

    public Song() {
        this.uploadDate = LocalDate.now();
    }

    public Song(String name, String genre, Double price, Artist artist) {
        this();
        this.name = name;
        this.genre = genre;
        this.price = price;
        this.artist = artist;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDate uploadDate) { this.uploadDate = uploadDate; }

    public String getPreviewClip() { return previewClip; }
    public void setPreviewClip(String previewClip) { this.previewClip = previewClip; }

    public String getSongImage() { return songImage; }
    public void setSongImage(String songImage) { this.songImage = songImage; }

    public String getAudioFile() { return audioFile; }
    public void setAudioFile(String audioFile) { this.audioFile = audioFile; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

    public Long getArtistId() {
        return artist != null ? artist.getUserId() : null;
    }
}
