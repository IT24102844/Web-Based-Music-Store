package com.app.musicstore.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "venue", length = 255)
    private String venue;

    @Column(name = "ticketPrice", nullable = false)
    private double ticketPrice;

    @Column(name = "imagePath", length = 255)
    private String imagePath;

    // FIXED: Add artistId field to match your database
    @Column(name = "artist")
    private Long artistId;

    // Optional: Relationship to User (for fetching artist details)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist", referencedColumnName = "id")
    private User artist;

    // Constructors
    public Event() {
    }

    public Event(String title, String description, LocalDate date, String venue, double ticketPrice, String imagePath,
            Long artistId) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.venue = venue;
        this.ticketPrice = ticketPrice;
        this.imagePath = imagePath;
        this.artistId = artistId;
    }

    // FIXED: Add getArtistId() method
    public Long getArtistId() {
        return artistId;
    }

    // FIXED: Add setArtistId() method
    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }

    // Getters and Setters for other fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public User getArtist() {
        return artist;
    }

    public void setArtist(User artist) {
        this.artist = artist;
        if (artist != null) {
            this.artistId = artist.getId();
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", ticketPrice=" + ticketPrice +
                ", venue='" + venue + '\'' +
                ", artistId=" + artistId +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}