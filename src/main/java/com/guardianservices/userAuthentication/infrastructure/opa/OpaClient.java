package com.guardianservices.userAuthentication.infrastructure.opa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for Open Policy Agent (OPA) integration
 * Used for fine-grained authorization decisions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpaClient {

    private final RestTemplate restTemplate;

    @Value("${opa.url:http://localhost:8181}")
    private String opaUrl;

    /**
     * Evaluates authorization policy for a user action
     *
     * @param user user information
     * @param resource resource being accessed
     * @param action action being performed
     * @return true if access is allowed
     */
    public boolean allowAccess(Map<String, Object> user, String resource, String action) {
        String policyUrl = opaUrl + "/v1/data/rbac/allow";

        Map<String, Object> input = new HashMap<>();
        input.put("user", user);
        input.put("resource", resource);
        input.put("action", action);

        Map<String, Object> request = Map.of("input", input);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    policyUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("result")) {
                Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
                return (boolean) result.getOrDefault("allow", false);
            }
        } catch (Exception e) {
            log.error("Error evaluating OPA policy: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Evaluates role-based access using OPA
     *
     * @param userRoles user's roles
     * @param requiredRole required role
     * @return true if user has required role
     */
    public boolean evaluateRoleAccess(String[] userRoles, String requiredRole) {
        String policyUrl = opaUrl + "/v1/data/rbac/role_check";

        Map<String, Object> input = new HashMap<>();
        input.put("user_roles", userRoles);
        input.put("required_role", requiredRole);

        Map<String, Object> request = Map.of("input", input);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    policyUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("result")) {
                Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
                return (boolean) result.getOrDefault("has_role", false);
            }
        } catch (Exception e) {
            log.error("Error evaluating OPA role policy: {}", e.getMessage());
        }

        return false;
    }
}
