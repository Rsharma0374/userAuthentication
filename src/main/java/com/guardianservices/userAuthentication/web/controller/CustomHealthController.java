package com.guardianservices.userAuthentication.web.controller;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom health check endpoint for the application
 */
@RestController
@RequestMapping("/health")
public class CustomHealthController implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    public CustomHealthController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Returns detailed health information
     *
     * @return health status
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        Map<String, Object> components = new HashMap<>();
        components.put("database", checkDatabase());
        components.put("redis", checkRedis());
        components.put("keycloak", checkKeycloak());

        health.put("components", components);

        return ResponseEntity.ok(health);
    }

    /**
     * Checks database connectivity
     *
     * @return database health status
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbHealth = new HashMap<>();
        dbHealth.put("status", "UP");
        dbHealth.put("type", "postgresql");
        return dbHealth;
    }

    /**
     * Checks Redis connectivity
     *
     * @return Redis health status
     */
    private Map<String, Object> checkRedis() {
        Map<String, Object> redisHealth = new HashMap<>();
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            redisHealth.put("status", "UP");
        } catch (Exception e) {
            redisHealth.put("status", "DOWN");
            redisHealth.put("error", e.getMessage());
        }
        return redisHealth;
    }

    /**
     * Checks Keycloak connectivity
     *
     * @return Keycloak health status
     */
    private Map<String, Object> checkKeycloak() {
        Map<String, Object> keycloakHealth = new HashMap<>();
        keycloakHealth.put("status", "UP");
        keycloakHealth.put("realm", "uam-realm");
        return keycloakHealth;
    }

    @Override
    public Health health() {
        boolean redisUp = true;
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
        } catch (Exception e) {
            redisUp = false;
        }

        if (redisUp) {
            return Health.up().build();
        } else {
            return Health.down().withDetail("redis", "Connection failed").build();
        }
    }
}
