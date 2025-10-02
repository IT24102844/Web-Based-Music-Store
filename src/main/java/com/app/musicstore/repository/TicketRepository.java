package com.app.musicstore.repository;

import com.app.musicstore.model.Ticket;
import com.app.musicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUser(User user);
    List<Ticket> findByUserOrderByPurchasedAtDesc(User user);
    Ticket findByTicketNumber(String ticketNumber);
}