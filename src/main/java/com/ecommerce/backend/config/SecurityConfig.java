package com.ecommerce.backend.config;

import com.ecommerce.backend.security.JwtAuthenticationFilter;
import com.ecommerce.backend.security.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwtUtil);

        http
            .csrf(csrf -> csrf.disable())

            .cors(Customizer.withDefaults())

            .httpBasic(httpBasic ->
                    httpBasic.disable())

            .formLogin(formLogin ->
                    formLogin.disable())

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    ))

            .authorizeHttpRequests(auth -> auth

                // =================================================
                // PUBLIC ENDPOINTS
                // =================================================

                .requestMatchers(
                        "/api/users/register",
                        "/api/users/login"
                ).permitAll()

                .requestMatchers(
                        "/error",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                ).permitAll()

                // Products can be viewed without login
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/products/**"
                ).permitAll()


                // =================================================
                // USER MANAGEMENT
                // =================================================

                .requestMatchers(
                        "/api/users/**"
                ).hasRole("ADMIN")


                // =================================================
                // PRODUCT MANAGEMENT
                // =================================================

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/products/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/products/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/products/**"
                ).hasRole("ADMIN")


                // =================================================
                // ORDERS
                // =================================================

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/orders"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/orders/my-orders"
                ).hasAnyRole("USER", "ADMIN")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/orders/user/**"
                ).hasAnyRole("USER", "ADMIN")

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/orders"
                ).hasAnyRole("USER", "ADMIN")

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/orders/checkout"
                ).hasAnyRole("USER", "ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/orders/*"
                ).hasAnyRole("USER", "ADMIN")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/orders/*/status"
                ).hasRole("ADMIN")


                // =================================================
                // CART
                // =================================================

                .requestMatchers(
                        "/api/cart/**"
                ).hasAnyRole("USER", "ADMIN")


                // =================================================
                // ADDRESSES
                // =================================================

                .requestMatchers(
                        "/api/addresses/**"
                ).hasAnyRole("USER", "ADMIN")


                // =================================================
                // PAYMENTS
                // =================================================

                .requestMatchers(
                        "/api/payments/**"
                ).hasAnyRole("USER", "ADMIN")


                // =================================================
                // EVERYTHING ELSE
                // =================================================

                .anyRequest().authenticated()
            )

            .exceptionHandling(exception ->
                    exception.authenticationEntryPoint(
                            (request, response, authException) ->
                                    response.setStatus(
                                            HttpServletResponse.SC_UNAUTHORIZED
                                    )
                    )
            )

            .addFilterBefore(
                    jwtFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}