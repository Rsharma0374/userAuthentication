package com.guardianservices.userAuthentication.infrastructure.cache;

import com.guardianservices.userAuthentication.domain.port.out.TokenCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis implementation of token cache port
 * Stores active user tokens with TTL
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisTokenCache implements TokenCachePort {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TOKEN_CACHE_PREFIX = "user:token:";

    /**
     * Saves token for a user with TTL
     *
     * @param key user ID as string
     * @param token JWT token
     * @param ttl time to live duration
     */
    @Override
    public void save(String key, String token, Duration ttl) {
        String cacheKey = TOKEN_CACHE_PREFIX + key;
        redisTemplate.opsForValue().set(cacheKey, token, ttl);
        log.debug("Token cached for key: {}", key);
    }

    /**
     * Finds token by user ID
     *
     * @param key user ID as string
     * @return optional containing token if found
     */
    @Override
    public Optional<String> find(String key) {
        String cacheKey = TOKEN_CACHE_PREFIX + key;
        Object token = redisTemplate.opsForValue().get(cacheKey);
        log.debug("Token retrieved for key: {}", key);
        return token != null ? Optional.of(token.toString()) : Optional.empty();
    }

    /**
     * Deletes token for a user
     *
     * @param key user ID as string
     */
    @Override
    public void delete(String key) {
        String cacheKey = TOKEN_CACHE_PREFIX + key;
        redisTemplate.delete(cacheKey);
        log.debug("Token deleted for key: {}", key);
    }

    /**
     * Checks if token exists for a user
     *
     * @param key user ID as string
     * @return true if token exists
     */
    @Override
    public boolean exists(String key) {
        String cacheKey = TOKEN_CACHE_PREFIX + key;
        Boolean exists = redisTemplate.hasKey(cacheKey);
        return Boolean.TRUE.equals(exists);
    }
}
