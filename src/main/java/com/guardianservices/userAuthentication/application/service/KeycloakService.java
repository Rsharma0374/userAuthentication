package com.guardianservices.userAuthentication.application.service;

import com.guardianservices.userAuthentication.domain.model.User;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.guardianservices.userAuthentication.conf.KeycloakConfig.KEYKLOAK_REALM;
import static com.guardianservices.userAuthentication.conf.KeycloakConfig.KEYKLOAK_SECRET_TYPE;

/**
 * Service for interacting with Keycloak Admin API
 * Handles user management, role assignment, and authentication operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    private final Keycloak keycloakAdminClient;

    @Autowired
    private InfisicalService infisicalService;

    /**
     * Creates a new user in Keycloak
     *
     * @param user application user entity
     * @param password user's password
     * @return created user representation from Keycloak
     */
    public UserRepresentation createUserInKeycloak(User user, String password) {
        log.debug("Creating user in Keycloak: {}", user.getUsername());
        try {
            UserRepresentation userRep = new UserRepresentation();
            userRep.setUsername(user.getUsername());
            userRep.setEmail(user.getEmail());
            userRep.setFirstName(user.getFirstName());
            userRep.setLastName(user.getLastName());
            userRep.setEnabled(user.getEnabled());
            userRep.setEmailVerified(user.getEmailVerified());

            // Set up credentials
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            userRep.setCredentials(Collections.singletonList(credential));

            // Create user in Keycloak
            Response response = getRealmResource().users().create(userRep);

            String responseBody = response.readEntity(String.class);

            log.error("Keycloak Status: {}", response.getStatus());
            log.error("Keycloak Response: {}", responseBody);

            if (response.getStatus() != 201) {
                throw new RuntimeException(
                        "Failed to create user in Keycloak. Status: "
                                + response.getStatus()
                                + " Response: "
                                + responseBody
                );
            }

            // Extract user ID from response
            String userId = getCreatedUserId(response);
            userRep.setId(userId);

            log.info("User created successfully in Keycloak with ID: {}", userId);
            return userRep;
        } catch (Exception e) {
            log.error("Exception occurred while creating creds in keykloak for user {} with cause {}", user.getUsername(), ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * Retrieves user by username from Keycloak
     *
     * @param username username to search for
     * @return user representation if found
     */
    public UserRepresentation getUserByUsername(String username) {
        log.debug("Retrieving user from Keycloak by username: {}", username);

        List<UserRepresentation> users = getRealmResource()
                .users()
                .search(username);

        return users.stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Assigns role to a user in Keycloak
     *
     * @param userId Keycloak user ID
     * @param roleName role name to assign
     */
    public void assignRoleToUser(String userId, String roleName) {
        log.debug("Assigning role '{}' to user: {}", roleName, userId);

        RealmResource realmResource = getRealmResource();

        // Get the role representation
        RoleRepresentation roleRep = realmResource.roles()
                .get(roleName)
                .toRepresentation();

        // Assign role to user
        UserResource userResource = realmResource.users().get(userId);
        userResource.roles().realmLevel()
                .add(Collections.singletonList(roleRep));

        log.info("Role '{}' assigned to user: {}", roleName, userId);
    }

    /**
     * Removes role from user in Keycloak
     *
     * @param userId Keycloak user ID
     * @param roleName role name to remove
     */
    public void removeRoleFromUser(String userId, String roleName) {
        log.debug("Removing role '{}' from user: {}", roleName, userId);

        RealmResource realmResource = getRealmResource();

        // Get the role representation
        RoleRepresentation roleRep = realmResource.roles()
                .get(roleName)
                .toRepresentation();

        // Remove role from user
        UserResource userResource = realmResource.users().get(userId);
        userResource.roles().realmLevel()
                .remove(Collections.singletonList(roleRep));

        log.info("Role '{}' removed from user: {}", roleName, userId);
    }

    /**
     * Removes a list of roles from a user in Keycloak.
     *
     * @param userId Keycloak user ID
     * @param roleNames list of role names to remove
     */
    public void removeRolesFromUser(String userId, List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return;
        }
        log.debug("Removing roles {} from user: {}", roleNames, userId);

        RealmResource realmResource = getRealmResource();
        UserResource userResource = realmResource.users().get(userId);

        List<RoleRepresentation> rolesToRemove = roleNames.stream()
                .map(roleName -> realmResource.roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());

        userResource.roles().realmLevel().remove(rolesToRemove);
        log.info("Roles {} removed from user: {}", roleNames, userId);
    }

    /**
     * Gets all roles of a user from Keycloak
     *
     * @param userId Keycloak user ID
     * @return list of role names
     */
    public List<String> getUserRoles(String userId) {
        log.debug("Retrieving roles for user: {}", userId);

        List<RoleRepresentation> roles = getRealmResource()
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .listEffective();

        return roles.stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());
    }

    /**
     * Updates user password in Keycloak
     *
     * @param userId Keycloak user ID
     * @param newPassword new password to set
     */
    public void updateUserPassword(String userId, String newPassword) {
        log.debug("Updating password for user: {}", userId);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false);

        getRealmResource()
                .users()
                .get(userId)
                .resetPassword(credential);

        log.info("Password updated for user: {}", userId);
    }

    /**
     * Deletes user from Keycloak
     *
     * @param userId Keycloak user ID
     */
    public void deleteUser(String userId) {
        log.debug("Deleting user from Keycloak: {}", userId);

        getRealmResource()
                .users()
                .delete(userId);

        log.info("User deleted from Keycloak: {}", userId);
    }

    /**
     * Creates a new role in Keycloak
     *
     * @param roleName name of the role
     * @param description role description
     */
    public void createRole(String roleName, String description) {
        log.debug("Creating role in Keycloak: {}", roleName);

        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName(roleName);
        roleRep.setDescription(description);

        getRealmResource()
                .roles()
                .create(roleRep);

        log.info("Role created in Keycloak: {}", roleName);
    }

    /**
     * Gets realm resource for Keycloak operations
     *
     * @return realm resource
     */
    private RealmResource getRealmResource() {
        return keycloakAdminClient.realm(infisicalService.getSecret(KEYKLOAK_REALM, KEYKLOAK_SECRET_TYPE));
    }

    /**
     * Extracts user ID from creation response
     *
     * @param response HTTP response from Keycloak
     * @return user ID
     */
    private String getCreatedUserId(Response response) {
        String location = response.getLocation().toString();
        return location.substring(location.lastIndexOf("/") + 1);
    }
}
