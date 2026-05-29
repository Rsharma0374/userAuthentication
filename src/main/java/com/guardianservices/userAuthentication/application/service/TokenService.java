package com.guardianservices.userAuthentication.application.service;

import com.guardianservices.userAuthentication.domain.port.out.TokenBlocklistPort;
import com.guardianservices.userAuthentication.domain.port.out.TokenCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Service for token management operations
 * Handles token validation, caching, and blocklisting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenCachePort tokenCachePort;
    private final TokenBlocklistPort tokenBlocklistPort;

    /**
     * Stores a user's token in cache
     */
    public void cacheToken(UUID userId, String token, long ttlSeconds) {
        log.debug("Caching token for user: {}", userId);
        tokenCachePort.save(userId.toString(), token, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * Invalidates a user's cached token (logout)
     */
    public void invalidateToken(UUID userId) {
        log.debug("Invalidating token for user: {}", userId);
        tokenCachePort.delete(userId.toString());
    }

    /**
     * Adds a token to the blocklist (logout/revocation)
     */
    public void blockToken(String token, long expirySeconds) {
        log.debug("Blocking token");
        tokenBlocklistPort.blockToken(token, Duration.ofSeconds(expirySeconds));
    }

    /**
     * Checks if a token is blocked (called by gateway via internal API)
     */
    public boolean isTokenBlocked(String token) {
        return tokenBlocklistPort.isTokenBlocked(token);
    }
}
