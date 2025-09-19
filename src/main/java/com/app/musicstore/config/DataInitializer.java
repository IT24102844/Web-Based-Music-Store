package com.app.musicstore.config;

import com.app.musicstore.model.entity.User;
import com.app.musicstore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {
            User artist = new User();
            artist.setUsername("artist1");
            artist.setPassword("pass");
            artist.setRole("ARTIST");
            userRepository.save(artist);

            User customer = new User();
            customer.setUsername("customer1");
            customer.setPassword("pass");
            customer.setRole("CUSTOMER");
            userRepository.save(customer);
        };
    }
}
