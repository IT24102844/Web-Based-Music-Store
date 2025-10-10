package com.app.musicstore.service;

import com.app.musicstore.model.Event;
import com.app.musicstore.model.Payment;
import com.app.musicstore.model.Ticket;
import com.app.musicstore.model.User;
import com.app.musicstore.repository.PaymentRepository;
import com.app.musicstore.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    public Payment processPayment(Event event, User user, Integer quantity, String paymentMethod) {
        try {
            System.out.println("💰 Processing payment for user: " + user.getUserId());
            System.out.println("🎫 Event: " + event.getTitle() + ", Quantity: " + quantity);

            // Calculate total amount
            Double totalAmount = event.getTicketPrice() * quantity;
            System.out.println("💵 Total amount: " + totalAmount);

            // Create payment record
            Payment payment = new Payment();
            payment.setEvent(event);
            payment.setUser(user);
            payment.setAmount(totalAmount);
            payment.setTicketQuantity(quantity);
            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionId("TXN-" + System.currentTimeMillis());
            payment.setPaymentStatus("COMPLETED");
            payment.setPaymentDate(java.time.LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);
            System.out.println("✅ Payment saved with ID: " + savedPayment.getPaymentId());

            // Generate tickets
            generateTickets(event, user, savedPayment, quantity);

            return savedPayment;

        } catch (Exception e) {
            System.err.println("❌ Payment processing error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    private void generateTickets(Event event, User user, Payment payment, Integer quantity) {
        try {
            System.out.println("🎫 Generating " + quantity + " tickets...");
            List<Ticket> tickets = new ArrayList<>();

            for (int i = 0; i < quantity; i++) {
                Ticket ticket = new Ticket();
                ticket.setEvent(event);
                ticket.setUser(user);
                ticket.setPayment(payment);
                ticket.setStatus("ACTIVE");
                ticket.setPurchasedAt(java.time.LocalDateTime.now());
                ticket.setTicketNumber("TKT-" + System.currentTimeMillis() + "-" + i);
                tickets.add(ticket);
                System.out.println("✅ Created ticket: " + ticket.getTicketNumber());
            }

            ticketRepository.saveAll(tickets);
            System.out.println("✅ All tickets saved successfully");

        } catch (Exception e) {
            System.err.println("❌ Ticket generation error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ticket generation failed: " + e.getMessage());
        }
    }

    public List<Payment> getUserPayments(User user) {
        return paymentRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Ticket> getUserTickets(User user) {
        return ticketRepository.findByUserOrderByPurchasedAtDesc(user);
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }
}