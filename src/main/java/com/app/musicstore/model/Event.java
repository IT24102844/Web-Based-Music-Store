package com.app.musicstore.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventID;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    private LocalDate date;

    @Column(length = 255)
    private String loction;

    @Column(nullable = false)
    private double ticket_price;

    @Column(length = 255)
    private String imagePath;

    // ✅ Relationship to Artist (FK stored in "artistId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artistId", referencedColumnName = "userId", nullable = false)
    private Artist artist;

    // Constructors
    public Event() {}

    public Event(String title, String description, LocalDate date, String venue, double ticketPrice, String imagePath, Artist artist) {
        this.name = title;
        this.description = description;
        this.date = date;
        this.loction = venue;
        this.ticket_price = ticketPrice;
        this.imagePath = imagePath;
        this.artist = artist;
    }

    // Getters and Setters
    public Long getId() { return eventID; }
    public void setId(Long id) { this.eventID = id; }

    public String getTitle() { return name; }
    public void setTitle(String title) { this.name = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getVenue() { return loction; }
    public void setVenue(String venue) { this.loction = venue; }

    public double getTicketPrice() { return ticket_price; }
    public void setTicketPrice(double ticketPrice) { this.ticket_price = ticketPrice; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + eventID +
                ", title='" + name + '\'' +
                ", ticketPrice=" + ticket_price +
                ", venue='" + loction + '\'' +
                ", artist=" + (artist != null ? artist.getUserId() : null) +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
