package com.eduverse.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // 1. Password encoder configuration
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Authentication manager setup
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 3. Security core filter chain configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Cross-Origin Resource Sharing (CORS) rules for React frontend
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))

                // Disable CSRF since APIs use stateless JWT tokens
                .csrf(csrf -> csrf.disable())

                // Set sessions to stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Set up route permissions and access guards.
                // NOTE: Spring Security evaluates these top-to-bottom and stops at the
                // first match, so the most specific paths must always come before the
                // broader ones underneath them (e.g. the admin force-assign rule must
                // be listed before the general /enrollments/** student rule, otherwise
                // the broader rule would win first and lock admins out).
                .authorizeHttpRequests(auth -> auth
                        // Public open endpoints
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/login").permitAll()
                        // Simulated payment gateway callback - a real deployment would verify
                        // a signing secret here instead of leaving this open.
                        .requestMatchers(HttpMethod.POST, "/orders/payment-webhook").permitAll()

                        // Explicit admin overrides (must precede the general /enrollments/** rule below).
                        // Fixed from the original "/enrollments/admin/force-assign/**", which never actually
                        // matched the real endpoint - Ant-style "/**" requires a trailing path segment, so it
                        // didn't match the exact "/enrollments/admin/force-assign" path the controller exposes.
                        .requestMatchers(HttpMethod.POST, "/enrollments/admin/force-assign").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users").hasAuthority("ROLE_ADMIN")

                        // Student enrollment, purchasing and progress endpoints.
                        // These GET matchers on /courses/*/... must come before the general
                        // GET /courses/** permitAll rule further down, or that broader rule
                        // would shadow them and make progress data public by accident.
                        .requestMatchers("/enrollments", "/enrollments/**").hasAuthority("ROLE_STUDENT")
                        .requestMatchers(HttpMethod.POST, "/orders").hasAuthority("ROLE_STUDENT")
                        .requestMatchers(HttpMethod.GET, "/orders/my").hasAuthority("ROLE_STUDENT")
                        .requestMatchers(HttpMethod.POST, "/lessons/*/complete").hasAuthority("ROLE_STUDENT")
                        .requestMatchers(HttpMethod.GET, "/courses/*/progress").hasAuthority("ROLE_STUDENT")
                        .requestMatchers(HttpMethod.POST, "/courses/*/reviews").hasAuthority("ROLE_STUDENT")

                        // Instructor and Admin course/curriculum management
                        .requestMatchers(HttpMethod.GET, "/courses/my").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/courses").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/courses/**").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/courses/*/publish").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/**").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/courses/*/modules").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/modules/*/lessons").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")

                        // Any authenticated user (student, instructor, or admin) can query course RAG search
                        .requestMatchers(HttpMethod.POST, "/courses/*/ask").authenticated()

                        // General public browsing: course catalog, course detail, curriculum, reviews.
                        // Placed after the more specific /courses/*/... rules above so it only
                        // catches what nothing more specific already matched.
                        .requestMatchers(HttpMethod.GET, "/courses/**").permitAll()

                        // Protect everything else
                        .anyRequest().authenticated()
                );

        // Add custom JWT filter block before standard username-password validation
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
