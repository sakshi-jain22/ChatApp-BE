package com.explorer.ChatApp.config;

import com.explorer.ChatApp.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        AuthenticationEntryPoint authenticationEntryPoint,
                        AccessDeniedHandler accessDeniedHandler,
                        CorsConfigurationSource corsConfigurationSource
        ) throws Exception {
                http
                                .csrf(csrf -> csrf.disable()) // Disable CSRF for testing APIs via Postman/Localhost
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/public/**", "/api/auth/register", "/api/auth/login", "/actuator/health",
                                                                "/ws/**", "/error")
                                                .permitAll() // Example: Public endpoints
                                                .requestMatchers(
                                                        "/api/auth/logout"
                                                ).authenticated()
                                                .anyRequest().authenticated() // FIX: Uncomment this so all other
                                                                              // requests require Basic Auth
                                )
                                .httpBasic(Customizer.withDefaults()) // Enable HTTP Basic Auth
                                .exceptionHandling(exceptionHandle -> exceptionHandle
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler))
                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Returns 401 when authentication is missing or invalid.
         */
        @Bean
        public AuthenticationEntryPoint authenticationEntryPoint() {
                return ((request, response, authException) -> {
                        System.out.println("AuthenticationEntryPoint: " + authException);
                        response.setStatus(
                                        HttpServletResponse.SC_UNAUTHORIZED);

                        response.setContentType("application/json");
                        response.getWriter().write("""
                                        {
                                            "status": 401,
                                            "error": "Unauthorized",
                                            "message": "Authentication is required"
                                        }
                                        """);
                });
        }

        /**
         * Returns 403 when an authenticated user lacks permission.
         */
        @Bean
        public AccessDeniedHandler accessDeniedHandler() {
                return ((request, response, accessDeniedException) -> {
                        System.out.println("AccessDeniedHandler: " + accessDeniedException);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("""
                                        {
                                          "status": 403,
                                          "error": "Forbidden",
                                          "message": "You do not have permission to access this resource"
                                        }
                                        """);
                });
        }
}
