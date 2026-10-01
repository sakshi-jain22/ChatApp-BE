package com.explorer.ChatApp.websocket;

import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.enums.UserStatus;
import com.explorer.ChatApp.security.CustomWebSocketPrincipal;
import com.explorer.ChatApp.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;

@Component
public class WebSocketPresenceListener {
    private final UserService userService;

    public WebSocketPresenceListener(UserService userService) {
        this.userService = userService;
    }

    @EventListener
    public void handleSessionDisconnect(
            SessionDisconnectEvent event
    ) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() == null) return;

        if(!(accessor.getUser() instanceof CustomWebSocketPrincipal principal)) return;

        Long userId = principal.getUserId();

        try {
            User user = userService.getUserById(userId);
            user.setStatus(UserStatus.OFFLINE);
            user.setLastSeen(Instant.now());

            userService.saveUser(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
