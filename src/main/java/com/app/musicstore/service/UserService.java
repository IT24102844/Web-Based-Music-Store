package com.app.musicstore.service;

import com.app.musicstore.model.*;
import com.app.musicstore.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ArtistService artistService;
    private final CustomerService customerService;
    private final CourseSellerService courseSellerService;
    private final InstrumentSellerService instrumentSellerService;

    // Repositories for cascade deletion
    private final com.app.musicstore.repository.UnifiedPaymentRepository unifiedPaymentRepository;
    private final com.app.musicstore.repository.SupportTicketRepository supportTicketRepository;
    private final com.app.musicstore.repository.TicketRepository ticketRepository;
    private final com.app.musicstore.repository.PaymentRepository paymentRepository;

    public UserService(UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            ArtistService artistService,
            CourseSellerService courseSellerService,
            InstrumentSellerService instrumentSellerService,
            CustomerService customerService,
            com.app.musicstore.repository.UnifiedPaymentRepository unifiedPaymentRepository,
            com.app.musicstore.repository.SupportTicketRepository supportTicketRepository,
            com.app.musicstore.repository.TicketRepository ticketRepository,
            com.app.musicstore.repository.PaymentRepository paymentRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.artistService = artistService;
        this.customerService = customerService;
        this.courseSellerService = courseSellerService;
        this.instrumentSellerService = instrumentSellerService;
        this.unifiedPaymentRepository = unifiedPaymentRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.ticketRepository = ticketRepository;
        this.paymentRepository = paymentRepository;
    }

    // Basic registration (no extra details)
    public User registerUser(User user) {
        userRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            throw new IllegalArgumentException("Email already exists");
        });

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // Registration with role-specific details
    @Transactional
    public User registerUserWithDetails(User user, Map<String, String> roleSpecificData) {
        try {
            System.out.println("=== USER SERVICE DEBUG ===");
            System.out.println("Starting registration for user: " + user.getEmail() + " with role: " + user.getRole());

            Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                User existing = existingUser.get();
                if (existing.getStatus() == Status.ACTIVE) {
                    throw new IllegalArgumentException("Email already exists");
                } else {
                    // Remove inactive user before re-registering
                    System.out.println("Found inactive user with email: " + user.getEmail() + ", deleting...");
                    deleteUser(existing.getUserId());
                }
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            User savedUser;

            switch (user.getRole()) {
                case ARTIST -> {
                    Artist artist = new Artist();
                    artist.setName(user.getName());
                    artist.setEmail(user.getEmail());
                    artist.setPassword(user.getPassword());
                    artist.setPhoneNo(user.getPhoneNo());
                    artist.setAddress(user.getAddress());
                    artist.setRole(Role.ARTIST);
                    artist.setStatus(user.getStatus());
                    artist.setCreatedAt(user.getCreatedAt());
                    artist.setUpdatedAt(user.getUpdatedAt());
                    artist.setStageName(roleSpecificData.get("stageName"));
                    artist.setGenre(roleSpecificData.get("genre"));
                    savedUser = artistService.save(artist);
                }
                case CUSTOMER -> {
                    Customer customer = new Customer();
                    customer.setName(user.getName());
                    customer.setEmail(user.getEmail());
                    customer.setPassword(user.getPassword());
                    customer.setPhoneNo(user.getPhoneNo());
                    customer.setAddress(user.getAddress());
                    customer.setRole(Role.CUSTOMER);
                    customer.setStatus(user.getStatus());
                    customer.setCreatedAt(user.getCreatedAt());
                    customer.setUpdatedAt(user.getUpdatedAt());
                    customer.setPreferences(roleSpecificData.get("preferences"));
                    savedUser = customerService.save(customer);
                }
                case COURSE_SELLER -> {
                    CourseSeller seller = new CourseSeller();
                    seller.setName(user.getName());
                    seller.setEmail(user.getEmail());
                    seller.setPassword(user.getPassword());
                    seller.setPhoneNo(user.getPhoneNo());
                    seller.setAddress(user.getAddress());
                    seller.setRole(Role.COURSE_SELLER);
                    seller.setStatus(user.getStatus());
                    seller.setCreatedAt(user.getCreatedAt());
                    seller.setUpdatedAt(user.getUpdatedAt());
                    seller.setExpertise(roleSpecificData.get("expertise"));
                    seller.setExpYears(Integer.parseInt(roleSpecificData.getOrDefault("expYears", "0")));
                    savedUser = courseSellerService.save(seller);
                }
                case ITEM_SELLER -> {
                    InstrumentSeller seller = new InstrumentSeller();
                    seller.setName(user.getName());
                    seller.setEmail(user.getEmail());
                    seller.setPassword(user.getPassword());
                    seller.setPhoneNo(user.getPhoneNo());
                    seller.setAddress(user.getAddress());
                    seller.setRole(Role.ITEM_SELLER);
                    seller.setStatus(user.getStatus());
                    seller.setCreatedAt(user.getCreatedAt());
                    seller.setUpdatedAt(user.getUpdatedAt());
                    seller.setStoreName(roleSpecificData.get("storeName"));
                    seller.setStoreLocation(roleSpecificData.get("location"));
                    savedUser = instrumentSellerService.save(seller);
                }
                case ADMIN -> savedUser = userRepository.save(user);
                default -> throw new IllegalArgumentException("Unsupported role: " + user.getRole());
            }

            System.out.println("Registration successful for user: " + savedUser.getEmail());
            return savedUser;

        } catch (Exception e) {
            System.err.println("Registration failed for user: " + user.getEmail() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .filter(u -> u.getStatus() == Status.ACTIVE);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, User updatedUser) {
        var existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        Optional.ofNullable(updatedUser.getName()).ifPresent(existingUser::setName);
        Optional.ofNullable(updatedUser.getPhoneNo()).ifPresent(existingUser::setPhoneNo);
        Optional.ofNullable(updatedUser.getAddress()).ifPresent(existingUser::setAddress);
        Optional.ofNullable(updatedUser.getStatus()).ifPresent(existingUser::setStatus);

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getPassword() != null &&
                !updatedUser.getPassword().isEmpty() &&
                !updatedUser.getPassword().equals(existingUser.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Delete user (Hard Delete with cascade)
     * Permanently removes user and all associated data
     * 
     * @param userId The ID of the user to delete
     */
    @Transactional
    public void deleteUser(Long userId) {
        System.out.println("🗑️ Starting deletion for user ID: " + userId);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("Found user: " + user.getEmail() + " with role: " + user.getRole());

        try {
            // Step 1: Delete all foreign key related data first to avoid constraint
            // violations
            System.out.println("Step 1: Deleting foreign key related data...");

            // Delete unified payments (songs, courses, instruments, events purchased)
            var unifiedPayments = unifiedPaymentRepository.findByUserOrderByCreatedAtDesc(user);
            if (!unifiedPayments.isEmpty()) {
                System.out.println("   - Deleting " + unifiedPayments.size() + " unified payments");
                unifiedPaymentRepository.deleteAll(unifiedPayments);
            }

            // Support tickets are preserved for audit trail (user reference will be null)
            var supportTickets = supportTicketRepository.findByUserOrderByCreatedAtDesc(user);
            if (!supportTickets.isEmpty()) {
                System.out.println("   - Preserving " + supportTickets.size()
                        + " support tickets (setting user to null for audit trail)");
                // Set user to null instead of deleting tickets
                supportTickets.forEach(ticket -> {
                    ticket.setUser(null);
                    supportTicketRepository.save(ticket);
                });
            }

            // Delete event tickets
            var eventTickets = ticketRepository.findByUser(user);
            if (!eventTickets.isEmpty()) {
                System.out.println("   - Deleting " + eventTickets.size() + " event tickets");
                ticketRepository.deleteAll(eventTickets);
            }

            // Delete event payments
            var payments = paymentRepository.findByUser(user);
            if (!payments.isEmpty()) {
                System.out.println("   - Deleting " + payments.size() + " event payments");
                paymentRepository.deleteAll(payments);
            }

            System.out.println("   ✓ Foreign key data cleared");

            // Step 2: Delete role-specific data
            System.out.println("Step 2: Deleting role-specific data...");
            switch (user.getRole()) {
                case ARTIST -> {
                    System.out.println("   - Deleting Artist data");
                    artistService.findByUserId(userId)
                            .ifPresent(artist -> artistService.deleteById(artist.getUserId()));
                }
                case CUSTOMER -> {
                    System.out.println("   - Deleting Customer data");
                    customerService.findByUserId(userId)
                            .ifPresent(customer -> customerService.deleteById(customer.getUserId()));
                }
                case COURSE_SELLER -> {
                    System.out.println("   - Deleting CourseSeller data");
                    courseSellerService.findByUserId(userId)
                            .ifPresent(seller -> courseSellerService.deleteById(seller.getUserId()));
                }
                case ITEM_SELLER -> {
                    System.out.println("   - Deleting InstrumentSeller data");
                    instrumentSellerService.findByUserId(userId)
                            .ifPresent(seller -> instrumentSellerService.deleteById(seller.getUserId()));
                }
                case ADMIN -> System.out.println("   - Admin user - no role-specific data");
            }
            System.out.println("   ✓ Role-specific data deleted");

            // Step 3: Delete the user record
            System.out.println("Step 3: Deleting user record...");
            userRepository.deleteById(userId);
            System.out.println("✅ User deletion completed successfully for: " + userId);

        } catch (Exception e) {
            System.err.println("❌ Error during user deletion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    public void changeUserRole(Long userId, String newRole) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(Role.valueOf(newRole));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void changeUserStatus(Long userId, String status) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(Status.valueOf(status));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public boolean isEmailAvailable(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isEmpty() || user.get().getStatus() != Status.ACTIVE;
    }

    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public long getTotalUsersCount() {
        return userRepository.count();
    }

    public long getActiveUsersCount() {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() == Status.ACTIVE)
                .count();
    }

    public String saveProfileImage(Long userId, MultipartFile profileImage) throws IOException {
        // Create uploads directory if it doesn't exist
        String uploadDir = "uploads/profile/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate unique filename
        String originalFileName = profileImage.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + fileExtension;

        // Save file
        Path filePath = Paths.get(uploadDir + fileName);
        Files.copy(profileImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}
