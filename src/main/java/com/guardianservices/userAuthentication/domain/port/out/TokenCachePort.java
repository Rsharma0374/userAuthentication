package com.guardianservices.userAuthentication.domain.port.out;

import java.time.Duration;
import java.util.Optional;

/**
 * Port for token caching operations
 * Used for storing active user tokens
 */
public interface TokenCachePort {

    /**
     * Saves a token for a user
     *
     * @param key user identifier
     * @param token JWT token
     * @param ttl time to live
     */
    void save(String key, String token, Duration ttl);

    /**
     * Finds token by user key
     *
     * @param key user identifier
     * @return optional containing token if found
     */
    Optional<String> find(String key);

    /**
     * Deletes token for a user
     *
     * @param key user identifier
     */
    void delete(String key);

    /**
     * Checks if token exists for a user
     *
     * @param key user identifier
     * @return true if token exists
     */
    boolean exists(String key);
}
