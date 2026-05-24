package com.guardianservices.userAuthentication.infrastructure.cache;

import com.guardianservices.userAuthentication.domain.port.out.TokenBlocklistPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis implementation of token blocklist port
 * Stores revoked tokens with their expiration time
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisTokenBlocklist implements TokenBlocklistPort {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLOCKLIST_PREFIX = "token:blocklist:";

    /**
     * Adds token to blocklist with expiration
     *
     * @param token token to block
     * @param expiration duration the token should remain blocked
     */
    @Override
    public void add(String token, Duration expiration) {
        String blocklistKey = BLOCKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(blocklistKey, "blocked", expiration);
        log.debug("Token added to blocklist, expires in: {} seconds", expiration.getSeconds());
    }

    /**
     * Checks if token is in blocklist
     *
     * @param token token to check
     * @return true if token is blocked
     */
    @Override
    public boolean isBlocked(String token) {
        String blocklistKey = BLOCKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(blocklistKey);
        boolean blocked = Boolean.TRUE.equals(exists);

        if (blocked) {
            log.debug("Token found in blocklist");
        }

        return blocked;
    }

    /**
     * Removes token from blocklist
     *
     * @param token token to remove
     */
    @Override
    public void remove(String token) {
        String blocklistKey = BLOCKLIST_PREFIX + token;
        redisTemplate.delete(blocklistKey);
        log.debug("Token removed from blocklist");
    }
}
