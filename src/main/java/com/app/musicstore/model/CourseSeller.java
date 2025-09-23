package com.app.musicstore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CourseSeller")
@PrimaryKeyJoinColumn(name = "sellerId")
public class CourseSeller extends User {

    private String expertise;

    private Integer expYears;

    // Constructors
    public CourseSeller() {}

    public CourseSeller(String expertise, Integer expYears) {
        this.expertise = expertise;
        this.expYears = expYears;
    }

    // Getters and Setters
    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }

    public Integer getExpYears() { return expYears; }
    public void setExpYears(Integer expYears) { this.expYears = expYears; }
}