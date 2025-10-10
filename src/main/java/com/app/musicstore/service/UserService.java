package com.app.musicstore.service;

import com.app.musicstore.model.*;
import com.app.musicstore.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       ArtistService artistService,
                       CustomerService customerService,
                       CourseSellerService courseSellerService,
                       InstrumentSellerService instrumentSellerService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.artistService = artistService;
        this.customerService = customerService;
        this.courseSellerService = courseSellerService;
        this.instrumentSellerService = instrumentSellerService;
    }

    public User registerUser(User user) {
        userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already exists");
        });

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public User registerUserWithDetails(User user, Map<String, String> roleSpecificData) {
        try {
            System.out.println("=== USER SERVICE DEBUG ===");
            System.out.println("Starting registration for user: " + user.getEmail() + " with role: " + user.getRole());
            System.out.println("User name: " + user.getName());
            System.out.println("User password: " + (user.getPassword() != null ? "[PROVIDED]" : "NULL"));
            System.out.println("Role specific data: " + roleSpecificData);
            
            // Check if email already exists and is active
            Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                User existing = existingUser.get();
                if (existing.getStatus() == Status.ACTIVE) {
                    throw new IllegalArgumentException("Email already exists");
                } else {
                    // If user exists but is inactive, delete the inactive user first
                    System.out.println("Found inactive user with email: " + user.getEmail() + ", deleting...");
                    try {
                        hardDeleteUser(existing.getUserId());
                        System.out.println("Successfully deleted inactive user");
                    } catch (Exception e) {
                        System.err.println("Failed to delete inactive user: " + e.getMessage());
                        // Continue with registration anyway
                    }
                }
            }
            
            // Encode password and set timestamps
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
                System.out.println("Creating Customer object...");
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

                String preferences = roleSpecificData.get("preferences");
                System.out.println("Customer preferences: " + preferences);
                customer.setPreferences(preferences);

                System.out.println("Saving customer to database...");
                savedUser = customerService.save(customer);
                System.out.println("Customer saved successfully with ID: " + savedUser.getUserId());
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
            case ADMIN -> {
                // Admin is just a User
                savedUser = userRepository.save(user);
            }
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

        // Update fields using modern null-safe patterns
        Optional.ofNullable(updatedUser.getName()).ifPresent(existingUser::setName);
        Optional.ofNullable(updatedUser.getPhoneNo()).ifPresent(existingUser::setPhoneNo);
        Optional.ofNullable(updatedUser.getAddress()).ifPresent(existingUser::setAddress);
        Optional.ofNullable(updatedUser.getRole()).ifPresent(existingUser::setRole);
        Optional.ofNullable(updatedUser.getStatus()).ifPresent(existingUser::setStatus);

        // Handle email change safely (avoid unique constraint violation)
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()
                && !updatedUser.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        // Handle password separately
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        // Copy seller-specific fields if applicable
        if (existingUser instanceof InstrumentSeller existingSeller && updatedUser instanceof InstrumentSeller updatedSeller) {
            if (updatedSeller.getStoreName() != null && !updatedSeller.getStoreName().isEmpty()) {
                existingSeller.setStoreName(updatedSeller.getStoreName());
            }
            if (updatedSeller.getStoreLocation() != null && !updatedSeller.getStoreLocation().isEmpty()) {
                existingSeller.setStoreLocation(updatedSeller.getStoreLocation());
            }
            if (updatedSeller.getStoreAddress() != null && !updatedSeller.getStoreAddress().isEmpty()) {
                existingSeller.setStoreAddress(updatedSeller.getStoreAddress());
            }
            if (updatedSeller.getPaymentMethod() != null && !updatedSeller.getPaymentMethod().isEmpty()) {
                existingSeller.setPaymentMethod(updatedSeller.getPaymentMethod());
            }
            if (updatedSeller.getProfileImagePath() != null && !updatedSeller.getProfileImagePath().isEmpty()) {
                existingSeller.setProfileImagePath(updatedSeller.getProfileImagePath());
            }
        }

        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(Status.INACTIVE);
        userRepository.save(user);
    }

    public void hardDeleteUser(Long userId) {
        System.out.println("Starting hard delete for user ID: " + userId);
        
        // First delete from the specific user type table if it exists
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        System.out.println("Found user: " + user.getEmail() + " with role: " + user.getRole());
        
        // Delete from specific user type repositories based on role
        switch (user.getRole()) {
            case ARTIST -> {
                artistService.findByUserId(userId).ifPresent(artist -> {
                    System.out.println("Deleting artist record for user: " + userId);
                    artistService.deleteById(artist.getUserId());
                });
            }
            case CUSTOMER -> {
                customerService.findByUserId(userId).ifPresent(customer -> {
                    System.out.println("Deleting customer record for user: " + userId);
                    customerService.deleteById(customer.getUserId());
                });
            }
            case COURSE_SELLER -> {
                courseSellerService.findByUserId(userId).ifPresent(seller -> {
                    System.out.println("Deleting course seller record for user: " + userId);
                    courseSellerService.deleteById(seller.getUserId());
                });
            }
            case ITEM_SELLER -> {
                instrumentSellerService.findByUserId(userId).ifPresent(seller -> {
                    System.out.println("Deleting instrument seller record for user: " + userId);
                    instrumentSellerService.deleteById(seller.getUserId());
                });
            }
        }
        
        // Finally delete from the main users table
        System.out.println("Deleting main user record for user: " + userId);
        userRepository.deleteById(userId);
        System.out.println("Hard delete completed for user: " + userId);
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
}