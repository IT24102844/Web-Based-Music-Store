package com.app.musicstore.controller;

import com.app.musicstore.model.SupportTicket;
import com.app.musicstore.model.TicketStatus;
import com.app.musicstore.model.User;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.SupportTicketService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    // User submits a new ticket
    @GetMapping("/create")
    public String showCreateTicketForm(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("ticket", new SupportTicket());
        model.addAttribute("user", user);
        return "create-ticket";
    }

    @PostMapping("/create")
    public String createTicket(@RequestParam String message, RedirectAttributes redirectAttributes, Model model) {
        User user = getAuthenticatedUser();

        if (user == null) {
            return "redirect:/users/login";
        }

        try {
            SupportTicket supportTicket = supportTicketService.createTicket(user, message);
            redirectAttributes.addFlashAttribute("success",
                    "Ticket created successfully! Your complaint ID: " + supportTicket.getComplaintId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating ticket: " + e.getMessage());
        }

        return "redirect:/tickets/my-tickets";
    }

    // User views their tickets
    @GetMapping("/my-tickets")
    public String viewMyTickets(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }

        List<SupportTicket> supportTickets = supportTicketService.getUserTickets(user);
        model.addAttribute("tickets", supportTickets);
        model.addAttribute("user", user);
        return "my-tickets";
    }

    // Admin views all tickets
    @GetMapping("/admin")
    public String viewAllTickets(Model model) {
        User admin = getAuthenticatedUser();
        if (admin == null || admin.getRole() != com.app.musicstore.model.Role.ADMIN) {
            return "redirect:/users/login";
        }

        List<SupportTicket> supportTickets = supportTicketService.getAllTickets();
        long pendingCount = supportTicketService.getPendingTicketCount();

        model.addAttribute("tickets", supportTickets);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("statuses", TicketStatus.values());

        return "admin-tickets";
    }

    // Admin updates ticket status
    @PostMapping("/admin/update-status/{id}")
    public String updateTicketStatus(@PathVariable Long id,
                                     @RequestParam TicketStatus status,
                                     @RequestParam(required = false) String adminResponse,
                                     RedirectAttributes redirectAttributes) {
        User admin = getAuthenticatedUser();
        if (admin == null || admin.getRole() != com.app.musicstore.model.Role.ADMIN) {
            return "redirect:/users/login";
        }

        try {
            supportTicketService.updateTicketStatus(id, status, adminResponse);
            redirectAttributes.addFlashAttribute("success", "Ticket status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating ticket: " + e.getMessage());
        }

        return "redirect:/tickets/admin";
    }

    // Admin views ticket details
    @GetMapping("/admin/details/{id}")
    public String viewTicketDetails(@PathVariable Long id, Model model) {
        User admin = getAuthenticatedUser();
        if (admin == null || admin.getRole() != com.app.musicstore.model.Role.ADMIN) {
            return "redirect:/users/login";
        }

        Optional<SupportTicket> ticket = supportTicketService.getTicketById(id);
        if (ticket.isPresent()) {
            model.addAttribute("ticket", ticket.get());
            model.addAttribute("statuses", TicketStatus.values());
            return "ticket-details";
        } else {
            return "redirect:/tickets/admin?error=Ticket not found";
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser();
        }

        return null;
    }
}