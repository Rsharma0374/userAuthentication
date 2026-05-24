package com.guardianservices.userAuthentication.domain.port.out;

import java.time.Duration;

/**
 * Port for token blocklist operations
 * Used for storing revoked tokens
 */
public interface TokenBlocklistPort {

    /**
     * Adds token to blocklist
     *
     * @param token token to block
     * @param expiration duration token should remain blocked
     */
    void add(String token, Duration expiration);

    /**
     * Checks if token is blocked
     *
     * @param token token to check
     * @return true if token is blocked
     */
    boolean isBlocked(String token);

    /**
     * Removes token from blocklist
     *
     * @param token token to remove
     */
    void remove(String token);
}
