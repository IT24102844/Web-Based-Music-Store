// Ticket.java
package com.app.musicstore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role userRole;

    @Column(nullable = false, unique = true)
    private String complaintId;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.PENDING;

    private String adminResponse;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public SupportTicket() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public SupportTicket(User user, String message) {
        this();
        this.user = user;
        this.userRole = user.getRole();
        this.message = message;
        this.complaintId = generateComplaintId();
    }

    // Getters and Setters
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;

        if (user != null) {
            this.userRole = user.getRole();
        }
    }
    public Role getUserRole() { return userRole; }
    public void setUserRole(Role userRole) { this.userRole = userRole; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    private String generateComplaintId() {
        return "TKT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}