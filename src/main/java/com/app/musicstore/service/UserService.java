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
                seller.setLocation(roleSpecificData.get("location"));

                savedUser = instrumentSellerService.save(seller);
            }
            case ADMIN -> {
                // Admin is just a User
                savedUser = userRepository.save(user);
            }
            default -> throw new IllegalArgumentException("Unsupported role: " + user.getRole());
        }

        return savedUser;
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

        // Handle password separately
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
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
}