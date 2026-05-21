package com.userAuthentication.application.service;

import com.userAuthentication.application.command.AssignRoleCommand;
import com.userAuthentication.application.command.CreateRoleCommand;
import com.userAuthentication.application.command.RemoveRoleCommand;
import com.userAuthentication.domain.model.Role;
import com.userAuthentication.domain.model.User;
import com.userAuthentication.domain.port.out.RoleRepository;
import com.userAuthentication.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for role management operations
 * Handles role creation, assignment, and removal using Keycloak
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Creates a new role in Keycloak
     *
     * @param command create role command containing role name and description
     */
    @Transactional
    public void createRole(CreateRoleCommand command) {
        log.debug("Creating new role: {}", command.roleName());

        // Check if role already exists
        if (roleRepository.existsByName(command.roleName())) {
            throw new RuntimeException("Role already exists: " + command.roleName());
        }

        // Create role in Keycloak
        keycloakService.createRole(command.roleName(), command.description());

        // Save role to local database
        Role role = Role.builder()
                .name(command.roleName())
                .description(command.description())
                .build();

        roleRepository.save(role);

        log.info("Role created successfully: {}", command.roleName());
    }

    /**
     * Assigns a role to a user
     * Updates both Keycloak and local database
     *
     * @param command assign role command with user ID and role name
     */
    @Transactional
    public void assignRoleToUser(AssignRoleCommand command) {
        log.debug("Assigning role '{}' to user: {}", command.roleName(), command.userId());

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new RuntimeException("User not found: " + command.userId()));

        // Assign role in Keycloak
        keycloakService.assignRoleToUser(user.getKeycloakId(), command.roleName());

        // Update local user roles
        user.getRoles().add(command.roleName());
        userRepository.save(user);

        log.info("Role '{}' assigned to user: {}", command.roleName(), command.userId());
    }

    /**
     * Removes a role from a user
     * Updates both Keycloak and local database
     *
     * @param command remove role command with user ID and role name
     */
    @Transactional
    public void removeRoleFromUser(RemoveRoleCommand command) {
        log.debug("Removing role '{}' from user: {}", command.roleName(), command.userId());

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new RuntimeException("User not found: " + command.userId()));

        // Remove role in Keycloak
        keycloakService.removeRoleFromUser(user.getKeycloakId(), command.roleName());

        // Update local user roles
        user.getRoles().remove(command.roleName());
        userRepository.save(user);

        log.info("Role '{}' removed from user: {}", command.roleName(), command.userId());
    }

    /**
     * Retrieves all roles from Keycloak
     *
     * @return list of role names
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        log.debug("Retrieving all roles");
        // In a real implementation, fetch from Keycloak or cache
        return roleRepository.findAll();
    }

    /**
     * Retrieves roles assigned to a specific user
     *
     * @param userId user ID
     * @return set of role names
     */
    @Transactional(readOnly = true)
    public Set<String> getUserRoles(UUID userId) {
        log.debug("Retrieving roles for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return user.getRoles();
    }

    /**
     * Checks if a user has a specific role
     *
     * @param userId user ID
     * @param roleName role name to check
     * @return true if user has the role
     */
    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return user.getRoles().contains(roleName);
    }
}