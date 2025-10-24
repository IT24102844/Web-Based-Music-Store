package com.app.musicstore.controller;

import com.app.musicstore.model.*;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.EventService;
import com.app.musicstore.service.ArtistService;
import com.app.musicstore.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private PaymentService paymentService;

    // Use the external uploads directory
    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB limit

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ Created upload directory: " + uploadPath.toAbsolutePath());
            } else {
                System.out.println("📁 Upload directory already exists: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to create upload directory: " + e.getMessage());
        }
    }

    @Configuration
    public class WebConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Disable caching for static resources in development
            registry.addResourceHandler("/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(0);

            System.out.println("🔧 Cache disabled for static resources");
        }
    }

    // -------------------- Public: View only APPROVED events --------------------
    @GetMapping("/events")
    public String listEvents(Model model) {
        List<Event> events = eventService.getApprovedEvents();
        User currentUser = getCurrentUser();
        model.addAttribute("events", events);
        model.addAttribute("currentUser", currentUser);
        return "event_list";
    }

    // -------------------- Artist Only: Create new event --------------------
    @GetMapping("/artist/event/new")
    public String newEventForm(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        Optional<Artist> artistOpt = artistService.findByUserId(currentUser.getUserId());
        if (artistOpt.isEmpty()) {
            return "redirect:/users/login?error=artistRequired";
        }

        model.addAttribute("event", new Event());
        model.addAttribute("currentUser", currentUser);
        return "event_form";
    }

    @PostMapping("/artist/event/save")
    public String saveEvent(@ModelAttribute Event event,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        Optional<Artist> artistOpt = artistService.findByUserId(currentUser.getUserId());
        if (artistOpt.isEmpty()) {
            return "redirect:/users/login?error=artistRequired";
        }

        Artist artist = artistOpt.get();

        try {
            // Validate file size
            if (!imageFile.isEmpty() && imageFile.getSize() > MAX_FILE_SIZE) {
                model.addAttribute("error", "File size too large. Maximum size is 5MB.");
                model.addAttribute("event", event);
                model.addAttribute("currentUser", currentUser);
                return "event_form";
            }

            // Set default date if missing
            if (event.getDate() == null) {
                event.setDate(LocalDate.now().plusDays(7));
            }

            // Handle image upload
            if (!imageFile.isEmpty()) {
                String fileName = saveImageFile(imageFile);
                String imagePath = "/uploads/" + fileName;
                event.setImagePath(imagePath);

                System.out.println("🖼️ Image uploaded: " + fileName);
                System.out.println("📁 Saved to: " + UPLOAD_DIR + fileName);
                System.out.println("🔗 Image path: " + imagePath);
            } else {
                System.out.println("ℹ️ No image file provided for event: " + event.getTitle());
                event.setImagePath(null);
            }

            // Create event - this will set status to PENDING
            eventService.createEvent(event, artist);
            return "redirect:/artist/events?success=Event created successfully! Waiting for admin approval.";

        } catch (IOException e) {
            model.addAttribute("error", "Error uploading image: " + e.getMessage());
            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);
            return "event_form";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create event: " + e.getMessage());
            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);
            return "event_form";
        }
    }

    // -------------------- Artist Only: My events --------------------
    @GetMapping("/artist/events")
    public String artistEvents(Model model,
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String error) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        Optional<Artist> artistOpt = artistService.findByUserId(currentUser.getUserId());
        if (artistOpt.isEmpty()) {
            return "redirect:/users/login?error=artistRequired";
        }

        Artist artist = artistOpt.get();
        List<Event> artistEvents = eventService.getArtistEvents(artist);

        // Calculate status counts
        long pendingCount = artistEvents.stream()
                .filter(event -> event.getStatus() == EventStatus.PENDING)
                .count();

        long approvedCount = artistEvents.stream()
                .filter(event -> event.getStatus() == EventStatus.APPROVED)
                .count();

        long rejectedCount = artistEvents.stream()
                .filter(event -> event.getStatus() == EventStatus.REJECTED)
                .count();

        // Debug output
        System.out.println("📊 Event Statistics:");
        System.out.println("   Total Events: " + artistEvents.size());
        System.out.println("   Pending: " + pendingCount);
        System.out.println("   Approved: " + approvedCount);
        System.out.println("   Rejected: " + rejectedCount);

        model.addAttribute("events", artistEvents);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);

        if (success != null)
            model.addAttribute("success", success);
        if (error != null)
            model.addAttribute("error", error);

        return "artist_events";
    }

    // -------------------- Artist Only: Edit event --------------------
    @GetMapping("/artist/event/edit/{id}")
    public String editEvent(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        Optional<Artist> artistOpt = artistService.findByUserId(currentUser.getUserId());
        if (artistOpt.isEmpty()) {
            return "redirect:/users/login?error=artistRequired";
        }

        Artist artist = artistOpt.get();
        Event event = eventService.getEventById(id);

        if (event == null || !event.getArtist().getUserId().equals(artist.getUserId())) {
            return "redirect:/artist/events?error=notFound";
        }

        model.addAttribute("event", event);
        model.addAttribute("currentUser", currentUser);
        return "event_form";
    }

    @PostMapping("/artist/event/update/{id}")
    public String updateEvent(@PathVariable("id") Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("date") LocalDate date,
            @RequestParam("venue") String venue,
            @RequestParam("ticketPrice") Double ticketPrice,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/users/login?error=artistRequired";
        }

        Optional<Artist> artistOpt = artistService.findByUserId(currentUser.getUserId());
        if (artistOpt.isEmpty()) {
            return "redirect:/users/login?error=artistRequired";
        }

        Artist artist = artistOpt.get();

        try {
            Event existingEvent = eventService.getEventById(id);
            if (existingEvent == null || !existingEvent.getArtist().getUserId().equals(artist.getUserId())) {
                return "redirect:/artist/events?error=unauthorized";
            }

            // Validate file size for update
            if (imageFile != null && !imageFile.isEmpty() && imageFile.getSize() > MAX_FILE_SIZE) {
                model.addAttribute("error", "File size too large. Maximum size is 5MB.");
                model.addAttribute("event", existingEvent);
                model.addAttribute("currentUser", currentUser);
                return "event_form";
            }

            // Update fields
            existingEvent.setTitle(title);
            existingEvent.setDescription(description);
            existingEvent.setDate(date);
            existingEvent.setVenue(venue);
            existingEvent.setTicketPrice(ticketPrice);

            // Handle image update only if a new file is provided
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = saveImageFile(imageFile);
                String imagePath = "/uploads/" + fileName;
                existingEvent.setImagePath(imagePath);
                System.out.println("🔄 Updated image for event " + id + ": " + imagePath);
            }

            // When artist updates event, set status back to PENDING for admin review
            existingEvent.setStatus(EventStatus.PENDING);

            eventService.updateEvent(existingEvent);
            return "redirect:/artist/events?success=Event updated successfully! Waiting for admin approval.";

        } catch (Exception e) {
            model.addAttribute("error", "Error updating event: " + e.getMessage());
            Event event = eventService.getEventById(id);
            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);
            return "event_form";
        }
    }

    // -------------------- Artist Only: Delete event --------------------
    @GetMapping("/artist/event/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            Optional<Artist> artistOpt = artistService.findByUserId(currentUser.getUserId());
            if (artistOpt.isPresent()) {
                Artist artist = artistOpt.get();
                Event event = eventService.getEventById(id);
                if (event != null && event.getArtist().getUserId().equals(artist.getUserId())) {
                    // Delete the image file from filesystem
                    if (event.getImagePath() != null && !event.getImagePath().isEmpty()) {
                        deleteImageFile(event.getImagePath());
                    }
                    eventService.deleteEvent(id);
                    System.out.println("🗑️ Deleted event: " + id);
                }
            }
        }
        return "redirect:/artist/events";
    }

    // -------------------- Payment: Ticket Purchase --------------------
    @GetMapping("/events/{id}/tickets")
    public String purchaseTicketsForm(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/users/login?error=loginRequired";
        }

        Event event = eventService.getEventById(id);
        if (event == null || event.getStatus() != EventStatus.APPROVED) {
            return "redirect:/events?error=eventNotFound";
        }

        model.addAttribute("event", event);
        model.addAttribute("currentUser", currentUser);
        return "ticket_purchase";
    }

    @PostMapping("/events/{id}/process-payment")
    public String processPayment(@PathVariable Long id,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("cardNumber") String cardNumber,
            @RequestParam("expiryDate") String expiryDate,
            @RequestParam("cvv") String cvv,
            @RequestParam("cardholderName") String cardholderName,
            Model model) {

        System.out.println("🔄 Processing payment for event: " + id);
        System.out.println("📦 Quantity: " + quantity);
        System.out.println("💳 Payment Method: " + paymentMethod);

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            System.out.println("❌ User not logged in");
            return "redirect:/users/login?error=loginRequired";
        }

        Event event = eventService.getEventById(id);
        if (event == null || event.getStatus() != EventStatus.APPROVED) {
            System.out.println("❌ Event not found or not approved: " + id);
            return "redirect:/events?error=eventNotFound";
        }

        try {
            // Validate quantity
            if (quantity == null || quantity < 1 || quantity > 10) {
                System.out.println("❌ Invalid quantity: " + quantity);
                model.addAttribute("error", "Please select between 1 and 10 tickets");
                model.addAttribute("event", event);
                model.addAttribute("currentUser", currentUser);
                return "ticket_purchase";
            }

            // Validate card details
            String validationError = validateCardDetails(cardholderName, cardNumber, expiryDate, cvv);
            if (validationError != null) {
                System.out.println("❌ Card validation failed: " + validationError);
                model.addAttribute("error", validationError);
                model.addAttribute("event", event);
                model.addAttribute("currentUser", currentUser);
                return "ticket_purchase";
            }

            // Process payment
            System.out.println("✅ Processing payment with service...");
            Payment payment = paymentService.processPayment(event, currentUser, quantity, paymentMethod);
            System.out.println("✅ Payment processed successfully. Payment ID: " + payment.getPaymentId());

            return "redirect:/payment/success?paymentId=" + payment.getPaymentId();

        } catch (Exception e) {
            System.err.println("❌ Payment processing failed: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Payment failed: " + e.getMessage());
            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);
            return "ticket_purchase";
        }
    }

    // -------------------- Payment Success --------------------
    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam Long paymentId, Model model) {
        System.out.println("🔄 Loading payment success page for payment: " + paymentId);

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            System.out.println("❌ User not logged in for payment success");
            return "redirect:/users/login";
        }

        Payment payment = paymentService.getPaymentById(paymentId);
        if (payment == null) {
            System.out.println("❌ Payment not found: " + paymentId);
            return "redirect:/events?error=paymentNotFound";
        }

        // Check if payment belongs to current user
        if (!payment.getUser().getUserId().equals(currentUser.getUserId())) {
            System.out.println("❌ Unauthorized access to payment: " + paymentId);
            return "redirect:/events?error=unauthorized";
        }

        List<Ticket> tickets = paymentService.getUserTickets(currentUser).stream()
                .filter(ticket -> ticket.getPayment().getPaymentId().equals(paymentId))
                .toList();

        System.out.println("✅ Found " + tickets.size() + " tickets for payment: " + paymentId);

        model.addAttribute("payment", payment);
        model.addAttribute("tickets", tickets);
        model.addAttribute("currentUser", currentUser);
        return "payment_success";
    }

    // -------------------- User Tickets --------------------
    @GetMapping("/my-tickets")
    public String myTickets(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/users/login";
        }

        List<Ticket> tickets = paymentService.getUserTickets(currentUser);
        List<Payment> payments = paymentService.getUserPayments(currentUser);

        model.addAttribute("tickets", tickets);
        model.addAttribute("payments", payments);
        model.addAttribute("currentUser", currentUser);
        return "my_tickets";
    }

    // -------------------- Ticket Download Functionality --------------------
    @GetMapping("/tickets/download/{paymentId}")
    public void downloadTickets(@PathVariable Long paymentId, HttpServletResponse response) {
        System.out.println("📥 Download request for payment: " + paymentId);

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Please log in to download tickets");
                return;
            }

            Payment payment = paymentService.getPaymentById(paymentId);
            if (payment == null) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "Payment not found");
                return;
            }

            // Check if payment belongs to current user
            if (!payment.getUser().getUserId().equals(currentUser.getUserId())) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Access denied");
                return;
            }

            List<Ticket> tickets = paymentService.getUserTickets(currentUser).stream()
                    .filter(ticket -> ticket.getPayment().getPaymentId().equals(paymentId))
                    .toList();

            if (tickets.isEmpty()) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "No tickets found for this payment");
                return;
            }

            // Set response headers for text file download
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            String filename = "tickets-" + paymentId + "-" + System.currentTimeMillis() + ".txt";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            // Generate ticket content
            String ticketContent = generateTicketContent(payment, tickets);
            response.getWriter().write(ticketContent);
            response.getWriter().flush();

            System.out.println("✅ Tickets downloaded successfully for payment: " + paymentId);

        } catch (Exception e) {
            System.err.println("❌ Error downloading tickets: " + e.getMessage());
            try {
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error generating tickets");
            } catch (IOException ex) {
                System.err.println("❌ Failed to send error response: " + ex.getMessage());
            }
        }
    }

    @GetMapping("/tickets/print/{paymentId}")
    public String printTickets(@PathVariable Long paymentId, Model model) {
        System.out.println("🖨️ Print view for payment: " + paymentId);

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/users/login";
        }

        Payment payment = paymentService.getPaymentById(paymentId);
        if (payment == null) {
            return "redirect:/my-tickets?error=paymentNotFound";
        }

        // Check if payment belongs to current user
        if (!payment.getUser().getUserId().equals(currentUser.getUserId())) {
            return "redirect:/my-tickets?error=unauthorized";
        }

        List<Ticket> tickets = paymentService.getUserTickets(currentUser).stream()
                .filter(ticket -> ticket.getPayment().getPaymentId().equals(paymentId))
                .toList();

        model.addAttribute("payment", payment);
        model.addAttribute("tickets", tickets);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("printView", true); // Flag for print-specific styling

        return "ticket_print";
    }

    // -------------------- Helper Methods --------------------
    private String generateTicketContent(Payment payment, List<Ticket> tickets) {
        StringBuilder content = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        content.append("🎵 TuneWave - Your Event Tickets\n");
        content.append("================================\n\n");
        content.append("Order Details:\n");
        content.append("--------------\n");
        content.append("Transaction ID: ").append(payment.getTransactionId()).append("\n");
        content.append("Order Date: ").append(payment.getPaymentDate().format(formatter)).append("\n");
        content.append("Total Amount: LKR ").append(String.format("%.2f", payment.getAmount())).append("\n");
        content.append("Payment Method: ").append(payment.getPaymentMethod()).append("\n\n");

        content.append("Event Details:\n");
        content.append("--------------\n");
        content.append("Event: ").append(payment.getEvent().getTitle()).append("\n");
        content.append("Date: ").append(payment.getEvent().getDate()).append("\n");
        content.append("Venue: ").append(payment.getEvent().getVenue()).append("\n");
        content.append("Artist: ").append(payment.getEvent().getArtist().getStageName()).append("\n\n");

        content.append("Your Tickets (").append(tickets.size()).append("):\n");
        content.append("----------------").append("-".repeat(String.valueOf(tickets.size()).length())).append("\n\n");

        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            content.append("Ticket #").append(i + 1).append(":\n");
            content.append("  Ticket Number: ").append(ticket.getTicketNumber()).append("\n");
            content.append("  Status: ").append(ticket.getStatus()).append("\n");
            content.append("  Purchased: ").append(ticket.getPurchasedAt().format(formatter)).append("\n");
            content.append("  Seat: General Admission\n"); // You can add seat info if available
            content.append("  Barcode: ████████████████████████████████████████\n");
            content.append("  ").append(ticket.getTicketNumber()).append("\n");
            content.append("  ████████████████████████████████████████\n");

            if (i < tickets.size() - 1) {
                content.append("\n────────────────────────────────────────\n\n");
            }
        }

        content.append("\nImportant Information:\n");
        content.append("---------------------\n");
        content.append("• Please bring this ticket and valid ID to the event\n");
        content.append("• Tickets are non-transferable and non-refundable\n");
        content.append("• Doors open 1 hour before event start time\n");
        content.append("• For assistance, contact support@tunewave.com\n\n");

        content.append("Thank you for choosing TuneWave! 🎶\n");

        return content.toString();
    }

    private String validateCardDetails(String cardholderName, String cardNumber, String expiryDate, String cvv) {
        System.out.println("🔍 Validating card details...");

        if (cardholderName == null || cardholderName.trim().isEmpty()) {
            return "Cardholder name is required";
        }

        // Remove spaces from card number for validation
        String cleanCardNumber = cardNumber.replace(" ", "");
        if (cleanCardNumber.length() != 16 || !cleanCardNumber.matches("\\d+")) {
            return "Please enter a valid 16-digit card number";
        }

        if (!expiryDate.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            return "Please enter a valid expiry date in MM/YY format";
        }

        if (!cvv.matches("\\d{3,4}")) {
            return "Please enter a valid CVV (3 or 4 digits)";
        }

        System.out.println("✅ Card details validated successfully");
        return null; // No errors
    }

    // -------------------- Helper: Save image file --------------------
    private String saveImageFile(MultipartFile imageFile) throws IOException {
        String originalFileName = imageFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        }

        String fileName = System.currentTimeMillis() + fileExtension;
        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, imageFile.getBytes());

        return fileName;
    }

    // -------------------- Helper: Delete image file --------------------
    private void deleteImageFile(String imagePath) {
        try {
            if (imagePath != null && imagePath.startsWith("/uploads/")) {
                String fileName = imagePath.substring("/uploads/".length());
                Path filePath = Paths.get(UPLOAD_DIR, fileName);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    System.out.println("🗑️ Deleted image file: " + filePath.toAbsolutePath());
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to delete image file: " + e.getMessage());
        }
    }

    // -------------------- Helper: Get current logged-in user --------------------
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        return null;
    }
}