package com.example.enrollmentservice.config;

import com.example.enrollmentservice.config.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ GET all enrollments - only ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/enrollments").hasRole("ADMIN")

                        // ✅ Get enrollments by userId - STUDENT and INSTRUCTOR can view their own
                        .requestMatchers(HttpMethod.GET, "/api/enrollments/user/**").hasAnyRole("STUDENT", "INSTRUCTOR")

                        // ✅ Check if enrolled - both STUDENT and INSTRUCTOR
                        .requestMatchers(HttpMethod.GET, "/api/enrollments/check").hasAnyRole("STUDENT", "INSTRUCTOR")

                        // ✅ Enroll in course - STUDENT and INSTRUCTOR
                        .requestMatchers(HttpMethod.POST, "/api/enrollments").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/enrollments").hasRole("INSTRUCTOR")

                        // ✅ Unenroll - STUDENT and INSTRUCTOR
                        .requestMatchers(HttpMethod.DELETE, "/api/enrollments/**").hasAnyRole("STUDENT", "INSTRUCTOR")
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 🔐 Everything else must be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
