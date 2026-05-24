package com.guardianservices.userAuthentication.infrastructure.opa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Evaluator for role-based permissions using OPA
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpaRoleEvaluator {

    private final OpaClient opaClient;

    /**
     * Evaluates if user has permission for specific action on resource
     *
     * @param userId user ID
     * @param roles user roles
     * @param resource resource name
     * @param action action name
     * @return true if permitted
     */
    public boolean hasPermission(String userId, Set<String> roles, String resource, String action) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("roles", roles);

        return opaClient.allowAccess(user, resource, action);
    }

    /**
     * Evaluates if user has specific role
     *
     * @param roles user roles
     * @param requiredRole required role
     * @return true if user has role
     */
    public boolean hasRole(Set<String> roles, String requiredRole) {
        String[] rolesArray = roles.toArray(new String[0]);
        return opaClient.evaluateRoleAccess(rolesArray, requiredRole);
    }

    /**
     * Filters resources based on user permissions
     *
     * @param userId user ID
     * @param roles user roles
     * @param resources list of resources to filter
     * @param action action to check
     * @return filtered list of accessible resources
     */
    public Set<String> filterAccessibleResources(String userId, Set<String> roles,
                                                 Set<String> resources, String action) {
        // Implementation would filter resources based on OPA policies
        return resources.stream()
                .filter(resource -> hasPermission(userId, roles, resource, action))
                .collect(java.util.stream.Collectors.toSet());
    }
}
