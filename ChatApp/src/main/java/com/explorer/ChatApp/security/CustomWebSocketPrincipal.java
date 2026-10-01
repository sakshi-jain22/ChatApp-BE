package com.explorer.ChatApp.security;

import lombok.Getter;

import java.security.Principal;

@Getter
public class CustomWebSocketPrincipal implements Principal {
    private final Long userId;
    private final String email;

    public CustomWebSocketPrincipal(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    @Override
    public String getName() {
        return email;
    }

    public String getEmail() {
        return email;
    }
}
