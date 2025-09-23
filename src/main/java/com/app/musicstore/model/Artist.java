package com.app.musicstore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Artist")
@PrimaryKeyJoinColumn(name = "artistId")
public class Artist extends User {

    private String stageName;

    private String genre;

    // Constructors
    public Artist() {}

    public Artist(String stageName, String genre) {
        this.stageName = stageName;
        this.genre = genre;
    }

    // Getters and Setters
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
}