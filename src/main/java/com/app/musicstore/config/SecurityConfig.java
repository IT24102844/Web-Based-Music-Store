package com.app.musicstore.config;

import com.app.musicstore.service.SessionUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.io.IOException;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SessionUserService sessionUserService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/home", "/users/register", "/users/login", "/users/forgot-password",
                                "/css/**", "/js/**", "/images/**", "/uploads/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/artist/**").hasAuthority("ARTIST")
                        .requestMatchers("/dashboard/admin").hasAuthority("ADMIN")
                        .requestMatchers("/dashboard/artist").hasAuthority("ARTIST")
                        .requestMatchers("/dashboard/item-seller").hasAuthority("ITEM_SELLER")
                        .requestMatchers("/dashboard/course-seller").hasAuthority("COURSE_SELLER")
                        .requestMatchers("/dashboard/customer").hasAuthority("CUSTOMER")
                        .requestMatchers("/tickets/create", "/tickets/my-tickets").authenticated()
                        .requestMatchers("/tickets/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .usernameParameter("email")
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler())
                        .failureUrl("/users/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/users/login?logout=true")
                        .addLogoutHandler((request, response, authentication) -> {
                            var session = request.getSession(false);
                            if (session != null) {
                                sessionUserService.clearCachedUser(session);
                            }
                        })
                        .permitAll())
                .csrf(csrf -> csrf.disable()) // disable for development; enable in production
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
            public void onAuthenticationSuccess(HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication)
                    throws IOException, ServletException {

                // Initialize or refresh session cache
                var session = request.getSession(true);
                sessionUserService.refreshCachedUser(session);

                System.out.println("Login successful - cached user in session: " + authentication.getName());

                var authorities = authentication.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .toList();

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
                    response.sendRedirect("/dashboard");
                }
            }
        };
    }
}
