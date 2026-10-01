package com.explorer.ChatApp.security;

import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.exception.ResourceNotFoundException;
import com.explorer.ChatApp.service.UserService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final UserService userService;

    public WebSocketAuthenticationInterceptor(
            JwtService jwtService,
            UserService userService
    ) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        // = StompHeaderAccessor.wrap(message);
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        if(StompCommand.CONNECT.equals(command)) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer"))
                throw new ResourceNotFoundException(
                        "Missing or invalid Authorization header"
                );

            String token = authorizationHeader.substring(7);
            if(!jwtService.isTokenValid(token)) {
                throw new IllegalArgumentException(
                        "Invalid or expired JWT"
                );
            }

            String email = jwtService.extractEmail(token);
            Long userId = jwtService.extractUserId(token);
            User user = userService.getUserByEmail(email);

            // Optional consistency check:
            // The JWT user ID should match the database user.
            if (!user.getId().equals(userId)) {
                throw new IllegalArgumentException(
                        "Invalid user identity"
                );
            }

            Principal principal = new CustomWebSocketPrincipal(user.getId(), user.getEmail());
            accessor.setUser(principal);
        }
        return message;
    }
}
