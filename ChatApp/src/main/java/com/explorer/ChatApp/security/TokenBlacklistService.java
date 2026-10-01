package com.explorer.ChatApp.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revokeToken(
            String tokenId,
            Instant expiration
    ) {
        revokedTokens.put(tokenId, expiration);
    }

    public boolean isRevoked(String tokenId) {
        Instant expiration = revokedTokens.get(tokenId);

        if (expiration == null) {
            return false;
        }

        // Remove expired blacklist entries.
        if (expiration.isBefore(Instant.now())) {
            revokedTokens.remove(tokenId);
            return false;
        }

        return true;
    }
}
