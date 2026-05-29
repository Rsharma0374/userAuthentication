package com.guardianservices.userAuthentication.domain.port.out;

import java.time.Duration;

/**
 * Port for managing a blocklist of revoked tokens.
 * This provides an abstraction for blacklisting tokens until they expire.
 */
public interface TokenBlocklistPort {

    /**
     * Adds a token's unique identifier (e.g., JTI) to the blocklist for a specified duration.
     *
     * @param jti The unique identifier of the token to block.
     * @param expiry The duration for which the token should be blocked.
     */
    void blockToken(String jti, Duration expiry);

    /**
     * Checks if a token's unique identifier is present in the blocklist.
     *
     * @param jti The unique identifier of the token to check.
     * @return {@code true} if the token is blocked, {@code false} otherwise.
     */
    boolean isTokenBlocked(String jti);
}
