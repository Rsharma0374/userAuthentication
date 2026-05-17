package com.userAuthentication.domain.port.in;

import java.util.UUID;

/**
 * Token use case interface
 * Defines token management operations
 */
public interface TokenUseCase {

    /**
     * Caches token for user
     *
     * @param userId user ID
     * @param token JWT token
     * @param ttlSeconds time to live in seconds
     */
    void cacheToken(UUID userId, String token, long ttlSeconds);

    /**
     * Retrieves cached token
     *
     * @param userId user ID
     * @return cached token or null
     */
    String getCachedToken(UUID userId);

    /**
     * Invalidates user's token
     *
     * @param userId user ID
     */
    void invalidateToken(UUID userId);

    /**
     * Blocks a token
     *
     * @param token token to block
     * @param expirySeconds block duration
     */
    void blockToken(String token, long expirySeconds);

    /**
     * Validates token
     *
     * @param userId user ID
     * @param token token to validate
     * @return true if token is valid
     */
    boolean validateToken(UUID userId, String token);
}