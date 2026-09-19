package com.ecommerce.backend.config;

import com.ecommerce.backend.security.JwtAuthenticationFilter;
import com.ecommerce.backend.security.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Password Encryption
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Security Configuration
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwtUtil);

        http
                // Disable CSRF
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
.formLogin(formLogin -> formLogin.disable())

                // JWT authentication is stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        ))

                // API access rules
          .authorizeHttpRequests(auth -> auth

        // Public APIs
        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
        .requestMatchers(
        "/error",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**"
).permitAll()
        // Admin only
       .requestMatchers("/api/users/**").hasRole("ADMIN")
.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/**").hasAnyRole("USER", "ADMIN")
.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/orders/*/status")
.hasRole("ADMIN")

.requestMatchers("/api/orders/**")
.hasAnyRole("USER", "ADMIN")
.requestMatchers("/api/cart/**").hasAnyRole("USER", "ADMIN")
.requestMatchers("/api/addresses/**").hasAnyRole("USER", "ADMIN")
.requestMatchers("/api/payments/**").hasAnyRole("USER", "ADMIN")
.anyRequest().authenticated()
)

                // Handle unauthorized requests
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.setStatus(
                                                HttpServletResponse.SC_UNAUTHORIZED
                                        )
                        )
                )

                // Add JWT filter
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}