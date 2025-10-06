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
        return userRepository.findByEmail(email)
                .filter(u -> u.getStatus() == Status.ACTIVE) // check active status
                .map(CustomUserDetails::new) // wrap into UserDetails
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}

