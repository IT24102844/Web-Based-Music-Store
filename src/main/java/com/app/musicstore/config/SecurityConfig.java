package com.app.musicstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.io.IOException;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/home", "/users/register", "/users/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/artist/**").hasAuthority("ARTIST")
                        .requestMatchers("/dashboard/admin").hasAuthority("ADMIN")
                        .requestMatchers("/dashboard/artist").hasAuthority("ARTIST")
                        .requestMatchers("/dashboard/item-seller").hasAuthority("ITEM_SELLER")
                        .requestMatchers("/dashboard/course-seller").hasAuthority("COURSE_SELLER")
                        .requestMatchers("/dashboard/customer").hasAuthority("CUSTOMER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .usernameParameter("email")
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler()) // Use custom success handler
                        .failureUrl("/users/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/users/login?logout=true")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable()) // Enable for production
                .httpBasic(withDefaults());

        return http.build();
    }

    /**
     * Custom authentication success handler for role-based redirection
     */
    @Bean
    public AuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                // Get the authorities/roles of the logged-in user
                var authorities = authentication.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .toList();

                // Redirect based on role/authority
                if (authorities.contains("ADMIN")) {
                    response.sendRedirect("/dashboard/admin");
                } else if (authorities.contains("ARTIST")) {
                    response.sendRedirect("/dashboard/artist");
                } else if (authorities.contains("ITEM_SELLER")) {
                    response.sendRedirect("/dashboard/item-seller");
                } else if (authorities.contains("COURSE_SELLER")) {
                    response.sendRedirect("/dashboard/course-seller");
                } else if (authorities.contains("CUSTOMER")) {
                    response.sendRedirect("/dashboard/customer");
                } else {
                    // Default redirect for users with no specific role
                    response.sendRedirect("/dashboard");
                }
            }
        };
    }
}