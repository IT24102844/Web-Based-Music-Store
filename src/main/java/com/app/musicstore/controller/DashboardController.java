package com.app.musicstore.controller;

import com.app.musicstore.model.*;
import com.app.musicstore.security.CustomUserDetails;
import com.app.musicstore.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final ArtistService artistService;
    private final SongService songService;
    private final EventService eventService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final CourseService courseService;
    private final CourseSellerService courseSellerService;
    private final EnrollmentService enrollmentService;
    private final ProductService productService;

    public DashboardController(ArtistService artistService,
            SongService songService,
            EventService eventService,
            PaymentService paymentService,
            UserService userService,
            CourseService courseService,
            CourseSellerService courseSellerService,
            EnrollmentService enrollmentService,
            ProductService productService) {
        this.artistService = artistService;
        this.songService = songService;
        this.eventService = eventService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.courseService = courseService;
        this.courseSellerService = courseSellerService;
        this.enrollmentService = enrollmentService;
        this.productService = productService;
    }

    // -------------------- ADMIN DASHBOARD --------------------
    @GetMapping("/dashboard/admin")
    public String adminDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "admin-dashboard";
    }

    // -------------------- ARTIST DASHBOARD --------------------
    @GetMapping("/dashboard/artist")
    public String artistDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }

        Optional<Artist> artistOpt = artistService.findByUserId(user.getUserId());
        if (artistOpt.isPresent()) {
            Artist artist = artistOpt.get();
            List<Song> songs = songService.getSongsByArtist(artist);
            int songCount = songs.size();
            int eventsCount = eventService.getArtistEvents(artist).size();

            model.addAttribute("artist", artist);
            model.addAttribute("songCount", songCount);
            model.addAttribute("eventsCount", eventsCount);
            System.out.println("🎭 Loaded artist dashboard: " + artist.getStageName());
        } else {
            model.addAttribute("songCount", 0);
            model.addAttribute("eventsCount", 0);
            System.out.println("⚠️ No artist profile found for user: " + user.getUserId());
        }

        model.addAttribute("followerCount", 0); // Placeholder for now
        model.addAttribute("rating", 0.0);
        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);

        return "artist-dashboard";
    }

    // -------------------- ITEM SELLER DASHBOARD --------------------
    @GetMapping("/dashboard/item-seller")
    public String itemSellerDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "seller_dashboard";
    }

    // -------------------- COURSE SELLER DASHBOARD --------------------
    @GetMapping("/dashboard/course-seller")
    public String courseSellerDashboard(Model model) {
        User user = getFreshAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }

        // Get course seller specific data
        CourseSeller seller = courseSellerService.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Course seller profile not found"));

        List<Course> recentCourses = courseService.findByCourseSellerId(user.getUserId())
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // Calculate stats
        long totalCourses = courseService.findByCourseSellerId(user.getUserId()).size();
        long totalEnrollments = enrollmentService.getSellerEnrollments(user.getUserId()).size();
        long activeStudents = enrollmentService.getSellerEnrollments(user.getUserId())
                .stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();

        double totalRevenue = courseService.findByCourseSellerId(user.getUserId())
                .stream()
                .mapToDouble(course -> course.getPrice() * course.getEnrollments().size())
                .sum();

        model.addAttribute("user", user);
        model.addAttribute("recentCourses", recentCourses);
        model.addAttribute("stats", new DashboardStats(totalCourses, totalEnrollments, activeStudents, totalRevenue));

        return "course-seller-dashboard";
    }

    // -------------------- CUSTOMER DASHBOARD --------------------
    @GetMapping("/dashboard/customer")
    public String customerDashboard(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/users/login";
        }

        // Fetch limited items for horizontal display (4 items each)
        List<Song> featuredSongs = songService.getAllSongs().stream()
                .limit(4)
                .collect(Collectors.toList());

        List<Product> featuredInstruments = productService.getAllProducts().stream()
                .limit(4)
                .collect(Collectors.toList());

        List<Course> featuredCourses = courseService.findAll().stream()
                .limit(4)
                .collect(Collectors.toList());

        List<Event> featuredEvents = eventService.getApprovedEvents().stream()
                .limit(4)
                .collect(Collectors.toList());

        // Statistics
        List<Song> allSongs = songService.getAllSongs();
        List<String> genres = songService.getAllGenres();

        int totalSongs = allSongs.size();
        int totalGenres = genres.size();
        int totalArtists = (int) allSongs.stream()
                .map(song -> song.getArtist().getUserId())
                .distinct()
                .count();

        List<Ticket> userTickets = paymentService.getUserTickets(user);
        long eventsAttendedCount = userTickets.stream()
                .filter(ticket -> "ACTIVE".equals(ticket.getStatus()) || "USED".equals(ticket.getStatus()))
                .count();

        List<Enrollment> recentEnrollments = enrollmentService.getCustomerEnrollments(user.getUserId())
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("user", user);

        // Featured items for horizontal display
        model.addAttribute("featuredSongs", featuredSongs);
        model.addAttribute("featuredInstruments", featuredInstruments);
        model.addAttribute("featuredCourses", featuredCourses);
        model.addAttribute("featuredEvents", featuredEvents);

        // Statistics
        model.addAttribute("totalSongs", totalSongs);
        model.addAttribute("totalGenres", totalGenres);
        model.addAttribute("totalArtists", totalArtists);
        model.addAttribute("genres", genres);
        model.addAttribute("eventsAttendedCount", eventsAttendedCount);
        model.addAttribute("recentEnrollments", recentEnrollments);

        return "customer-dashboard";
    }

    // -------------------- AUTH HELPER --------------------
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        return null;
    }

    /**
     * Get FRESH user data from database instead of using the cached version
     */
    private User getFreshAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Get the user ID from the authenticated user, but fetch FRESH data from
            // database
            Long userId = userDetails.getUser().getUserId();
            return userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        return null;
    }

    /**
     * Inner class to hold dashboard statistics
     */
    public static class DashboardStats {
        private final long totalCourses;
        private final long totalEnrollments;
        private final long activeStudents;
        private final double totalRevenue;

        public DashboardStats(long totalCourses, long totalEnrollments, long activeStudents, double totalRevenue) {
            this.totalCourses = totalCourses;
            this.totalEnrollments = totalEnrollments;
            this.activeStudents = activeStudents;
            this.totalRevenue = totalRevenue;
        }

        public long getTotalCourses() {
            return totalCourses;
        }

        public long getTotalEnrollments() {
            return totalEnrollments;
        }

        public long getActiveStudents() {
            return activeStudents;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }
    }
}
