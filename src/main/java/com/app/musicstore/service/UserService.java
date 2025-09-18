package com.app.musicstore.service;

import com.app.musicstore.model.Role;
import com.app.musicstore.model.User;
import com.app.musicstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {
        if (user.getRole() != null) {
            user.setRole(Role.valueOf(user.getRole().name().toUpperCase()));
        } else {
            user.setRole(Role.CUSTOMER); // default
        }
        return userRepository.save(user);
    }


    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password)); // later → BCrypt
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setName(updatedUser.getName());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setStatus(updatedUser.getStatus());
        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser); // <-- this will trigger UPDATE not INSERT
    }
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
