package com.app.musicstore.security;

import com.app.musicstore.model.Status;
import com.app.musicstore.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Attempting to load user with email: " + email);

        return userRepository.findByEmail(email)
                .filter(user -> {
                    boolean isActive = user.getStatus() == Status.ACTIVE;
                    if (!isActive) {
                        System.out.println("❌ User account is not active: " + email);
                    }
                    return isActive;
                })
                .map(user -> {
                    System.out.println("✅ User found: " + user.getName() +
                            ", Status: " + user.getStatus() +
                            ", Role: " + user.getRole());
                    return new CustomUserDetails(user);
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found or inactive: " + email));
    }
}
