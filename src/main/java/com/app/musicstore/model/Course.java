package com.app.musicstore.model;

import jakarta.persistence.*;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    // File fields
    private String filePath;   // e.g., uploads/courses/123456_lesson1.pdf
    private String fileName;   // e.g., 123456_lesson1.pdf

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_seller_id")
    private CourseSeller courseSeller;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Enrollment> enrollments = new HashSet<>();

    // Constructors
    public Course() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Course(String title, String description, Double price, CourseSeller courseSeller) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.courseSeller = courseSeller;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        this.filePath = filePath;
        if (filePath != null && !filePath.isEmpty()) {
            this.fileName = Paths.get(filePath).getFileName().toString();
        } else {
            this.fileName = null;
        }
    }

    // Getters and Setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getFilePath() { return filePath; }
//    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
        // Automatically extract file name from path
        if (filePath != null && !filePath.isEmpty()) {
            this.fileName = Paths.get(filePath).getFileName().toString();
        } else {
            this.fileName = null;
        }
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public CourseSeller getCourseSeller() { return courseSeller; }
    public void setCourseSeller(CourseSeller courseSeller) { this.courseSeller = courseSeller; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<Enrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(Set<Enrollment> enrollments) { this.enrollments = enrollments; }
}
