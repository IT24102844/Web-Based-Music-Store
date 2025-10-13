// TicketService.java
package com.app.musicstore.service;

import com.app.musicstore.model.SupportTicket;
import com.app.musicstore.model.TicketStatus;
import com.app.musicstore.model.User;
import com.app.musicstore.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    public SupportTicketService(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }

    public SupportTicket createTicket(User user, String message) {
        SupportTicket supportTicket = new SupportTicket(user, message);
        return supportTicketRepository.save(supportTicket);
    }

    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SupportTicket> getUserTickets(User user) {
        return supportTicketRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<SupportTicket> getTicketsByStatus(TicketStatus status) {
        return supportTicketRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Optional<SupportTicket> getTicketById(Long id) {
        return supportTicketRepository.findById(id);
    }

    public Optional<SupportTicket> getTicketByComplaintId(String complaintId) {
        return supportTicketRepository.findByComplaintId(complaintId);
    }

    public SupportTicket updateTicketStatus(Long ticketId, TicketStatus status, String adminResponse) {
        Optional<SupportTicket> optionalTicket = supportTicketRepository.findById(ticketId);
        if (optionalTicket.isPresent()) {
            SupportTicket supportTicket = optionalTicket.get();
            supportTicket.setStatus(status);
            if (adminResponse != null && !adminResponse.trim().isEmpty()) {
                supportTicket.setAdminResponse(adminResponse);
            }
            return supportTicketRepository.save(supportTicket);
        }
        throw new RuntimeException("Ticket not found with id: " + ticketId);
    }

    public void deleteTicket(Long ticketId) {
        supportTicketRepository.deleteById(ticketId);
    }

    public long getPendingTicketCount() {
        return supportTicketRepository.countByStatus(TicketStatus.PENDING);
    }
}