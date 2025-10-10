// TicketRepository.java
package com.app.musicstore.repository;

import com.app.musicstore.model.SupportTicket;
import com.app.musicstore.model.TicketStatus;
import com.app.musicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUserOrderByCreatedAtDesc(User user);
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
    Optional<SupportTicket> findByComplaintId(String complaintId);

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.status = :status")
    long countByStatus(@Param("status") TicketStatus status);

    @Query("SELECT t FROM SupportTicket t WHERE t.user.userId = :userId ORDER BY t.createdAt DESC")
    List<SupportTicket> findByUserId(@Param("userId") Long userId);
}