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
        
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            System.out.println("User not found in database: " + email);
            throw new UsernameNotFoundException("User not found: " + email);
        }
        
        var user = userOpt.get();
        System.out.println("User found: " + user.getName() + ", Status: " + user.getStatus() + ", Role: " + user.getRole());
        
        if (user.getStatus() != Status.ACTIVE) {
            System.out.println("User account is not active: " + email);
            throw new UsernameNotFoundException("User account is not active: " + email);
        }
        
        return new CustomUserDetails(user);
    }
}

