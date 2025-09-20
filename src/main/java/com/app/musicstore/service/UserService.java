package com.app.musicstore.service;

import com.app.musicstore.model.User;
import com.app.musicstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User authenticate(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User foundUser = userOptional.get();
            // Simple password check (in production, use BCrypt)
            if (password != null && password.equals(foundUser.getPassword()) && foundUser.isActive()) {
                return foundUser;
            }
        }
        return null;
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public boolean isArtist(String email) {
        User user = getCurrentUser(email);
        return user != null && user.isArtist();
    }

    public List<User> getAllArtists() {
        return userRepository.findByRole("ARTIST");
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }
    }

    public List<User> findUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
}