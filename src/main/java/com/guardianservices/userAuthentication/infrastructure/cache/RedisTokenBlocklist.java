package com.guardianservices.userAuthentication.infrastructure.cache;

import com.guardianservices.userAuthentication.domain.port.out.TokenBlocklistPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Implementation of TokenBlocklistPort using Redis.
 * Stores blocked tokens with an expiration time to ensure the cache does not grow indefinitely.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisTokenBlocklist implements TokenBlocklistPort {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLOCKLIST_PREFIX = "token:blocklist:";

    @Override
    public void blockToken(String jti, Duration expiry) {
        if (jti == null || jti.isEmpty()) {
            log.warn("Attempted to block a null or empty JTI.");
            return;
        }
        String key = BLOCKLIST_PREFIX + jti;
        try {
            redisTemplate.opsForValue().set(key, "revoked", expiry);
            log.debug("Token JTI {} added to blocklist. Expires in {}", jti, expiry);
        } catch (Exception e) {
            log.error("Failed to add token JTI {} to blocklist. Error: {}", jti, e.getMessage(), e);
        }
    }

    @Override
    public boolean isTokenBlocked(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }
        String key = BLOCKLIST_PREFIX + jti;
        try {
            Boolean hasKey = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.error("Failed to check token blocklist for JTI {}. Assuming not blocked. Error: {}", jti, e.getMessage(), e);
            // In a highly secure system, returning false here might be a risk.
            // However, failing open (allowing access) during a cache outage is often preferred over a hard outage.
            return false;
        }
    }
}
