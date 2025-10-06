package com.app.musicstore.config;

import com.app.musicstore.model.Role;
import com.app.musicstore.model.Status;
import com.app.musicstore.model.User;
import com.app.musicstore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if admin already exists
        Optional<User> existingAdmin = userRepository.findByEmail("admin@musicstore.com");

        if (existingAdmin.isEmpty()) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("admin@musicstore.com");
            admin.setPassword(passwordEncoder.encode("admin123")); // Change this password!
            admin.setPhoneNo("000-000-0000");
            admin.setAddress("System Address");
            admin.setRole(Role.ADMIN);
            admin.setStatus(Status.ACTIVE);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            userRepository.save(admin);
            System.out.println("Default admin user created successfully");
        }
    }
}