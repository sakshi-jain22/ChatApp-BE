package com.explorer.ChatApp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;

        /*
         * Read the JWT from the Authorization header.
         *
         * Expected format:
         * Authorization: Bearer <JWT_TOKEN>
         */
        if (authorizationHeader != null &&
                authorizationHeader.startsWith("Bearer ")) {

            token = authorizationHeader.substring(7);

            try {
                email = jwtService.extractEmail(token);

            } catch (Exception exception) {

                /*
                 * Do not authenticate the request if the token
                 * cannot be parsed.
                 *
                 * Spring Security will handle authorization
                 * for protected endpoints.
                 */
                email = null;
            }
        }

        /*
         * Authenticate only when:
         *
         * 1. Email was extracted.
         * 2. No authentication is already present.
         * 3. Token is valid.
         */
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtService.isTokenValid(token)) {
            String tokenId = jwtService.extractTokenId(token);

            if (tokenBlacklistService.isRevoked(tokenId)) {
                filterChain.doFilter(request, response);
                return;
            }
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            boolean tokenIsValid = jwtService.isTokenValid(token);

            if(tokenIsValid && userDetails.getUsername().equalsIgnoreCase(email)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
