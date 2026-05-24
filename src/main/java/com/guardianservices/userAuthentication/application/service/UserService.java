package com.guardianservices.userAuthentication.application.service;

import com.guardianservices.userAuthentication.application.command.ChangePasswordCommand;
import com.guardianservices.userAuthentication.application.command.ResetPasswordCommand;
import com.guardianservices.userAuthentication.application.exception.KeycloakInteractionException;
import com.guardianservices.userAuthentication.application.exception.UserManagementException;
import com.guardianservices.userAuthentication.application.exception.UserNotFoundException;
import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.domain.model.User;
import com.guardianservices.userAuthentication.domain.port.in.UserManagementUseCase;
import com.guardianservices.userAuthentication.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for user management operations.
 * This service acts as an orchestrator between the local database and the external Identity Provider (Keycloak).
 * It ensures that user profiles, roles, and credentials remain synchronized across the ecosystem.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserManagementUseCase {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    /**
     * Retrieves a user entity by its unique identifier (UUID).
     * This is a read-only operation that fetches data exclusively from the local database.
     *
     * @param userId The UUID of the user to retrieve. Must not be null.
     * @return The User entity corresponding to the provided ID.
     * @throws UserNotFoundException If no user exists with the provided ID.
     * @throws UserManagementException If an unexpected database error occurs.
     */
    @Transactional(readOnly = true)
    @Override
    public User getUserById(UUID userId) {
        log.debug("Attempting to retrieve user by ID: {}", userId);
        try {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        } catch (UserNotFoundException e) {
            log.warn("User lookup failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error retrieving user with ID: {}", userId, e);
            throw new UserManagementException("Failed to retrieve user by ID", e);
        }
    }

    /**
     * Retrieves a user entity by their exact username.
     * This is a read-only operation querying the local database.
     *
     * @param username The exact string representation of the username. Must not be null or empty.
     * @return The User entity associated with the username.
     * @throws UserNotFoundException If no user exists with the provided username.
     * @throws UserManagementException If an unexpected error occurs during the lookup.
     */
    @Transactional(readOnly = true)
    @Override
    public User getUserByUsername(String username) {
        log.debug("Attempting to retrieve user by username: {}", username);
        try {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        } catch (UserNotFoundException e) {
            log.warn("User lookup failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error retrieving user with username: {}", username, e);
            throw new UserManagementException("Failed to retrieve user by username", e);
        }
    }

    /**
     * Retrieves a complete list of all users registered in the local database.
     * Warning: This operation can be memory-intensive on large datasets. Use pagination for production queries.
     *
     * @return A list containing all User entities. Returns an empty list if no users exist.
     * @throws UserManagementException If a database error occurs during retrieval.
     */
    @Transactional(readOnly = true)
    @Override
    public List<User> getAllUsers() {
        log.debug("Initiating retrieval of all users from the database.");
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to retrieve the list of all users.", e);
            throw new UserManagementException("An error occurred while fetching all users", e);
        }
    }

    /**
     * Retrieves a paginated subset of users from the local database.
     * Recommended approach for retrieving user lists to ensure consistent performance and low memory overhead.
     *
     * @param pageable Contains pagination information such as page number, page size, and sorting criteria.
     * @return A Page object containing the subset of User entities and pagination metadata.
     * @throws UserManagementException If a database error occurs during the paginated query.
     */
    @Transactional(readOnly = true)
    @Override
    public Page<User> getUsers(Pageable pageable) {
        log.debug("Retrieving users with pagination parameters: {}", pageable);
        try {
            return userRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Failed to retrieve paginated users with parameters: {}", pageable, e);
            throw new UserManagementException("An error occurred while fetching paginated users", e);
        }
    }

    /**
     * Updates basic profile information (First Name, Last Name, Email, Status) for an existing user.
     * Currently, this only updates the local database.
     * Note: Future iterations must synchronize these profile changes back to Keycloak.
     *
     * @param userId The UUID of the user being updated.
     * @param updatedUser A User object containing the new profile data.
     * @return The updated User entity as saved in the database.
     * @throws UserNotFoundException If the specified user ID does not exist.
     * @throws UserManagementException If an error occurs while saving the updated data.
     */
    @Transactional
    @Override
    public User updateUser(UUID userId, User updatedUser) {
        log.debug("Initiating profile update for user ID: {}", userId);
        try {
            User existingUser = getUserById(userId);

            // Update local database fields
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setEnabled(updatedUser.getEnabled());
            existingUser.setUpdatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(existingUser);
            log.info("Successfully updated profile for user ID: {}", userId);

            // TODO: Ensure data consistency by pushing profile changes to Keycloak via KeycloakService.
            return savedUser;

        } catch (UserNotFoundException e) {
            throw e; // Let the specific not found exception bubble up
        } catch (Exception e) {
            log.error("Failed to update profile for user ID: {}. Error: {}", userId, e.getMessage(), e);
            throw new UserManagementException("Failed to update user profile.", e);
        }
    }

    /**
     * Appends a new role and/or new products to a user's existing access profile.
     * This operation is strictly additive; existing roles or products are not removed.
     * Synchronizes role assignments with Keycloak to maintain access integrity.
     *
     * @param userId The UUID of the target user.
     * @param newRole The string identifier of the role to add. Ignored if null or already present.
     * @param products A list of Product enums to grant access to. Ignored if null. Duplicates are skipped.
     * @return The updated User entity reflecting the new access grants.
     * @throws UserNotFoundException If the user ID cannot be found.
     * @throws KeycloakInteractionException If communication with the Keycloak server fails during role assignment.
     * @throws UserManagementException If an error occurs while persisting the changes to the local database.
     */
    @Transactional
    @Override
    public User updateUserRoleAndProducts(UUID userId, String newRole, List<Product> products) {
        log.debug("Attempting to append role [{}] and products {} to user ID: {}", newRole, products, userId);
        
        User user = getUserById(userId);

        try {
            boolean isModified = false;

            // 1. Conditionally append new role and sync with Keycloak
            if (newRole != null && !user.getRoles().contains(newRole)) {
                log.debug("Assigning new role '{}' to user {} in Keycloak.", newRole, userId);
                keycloakService.assignRoleToUser(user.getKeycloakId(), newRole);
                user.getRoles().add(newRole);
                isModified = true;
            }

            // 2. Conditionally append new products
            if (products != null) {
                for (Product product : products) {
                    if (!user.getProducts().contains(product)) {
                        user.getProducts().add(product);
                        isModified = true;
                    }
                }
            }

            // 3. Persist changes if any modifications occurred
            if (isModified) {
                user.setUpdatedAt(LocalDateTime.now());
                User savedUser = userRepository.save(user);
                log.info("Successfully updated access profile for user ID: {}", userId);
                return savedUser;
            } else {
                log.debug("No new roles or products required appending for user ID: {}", userId);
                return user;
            }

        } catch (RuntimeException e) {
             // We catch RuntimeException specifically here because the Keycloak client often throws un-checked exceptions.
             // We want to wrap these to distinguish IdP failures from our internal DB failures.
            log.error("Failed to assign role to user {} in Keycloak. Reason: {}", userId, e.getMessage(), e);
            throw new KeycloakInteractionException("Failed to synchronize role assignment with Identity Provider.", e);
        } catch (Exception e) {
            log.error("Unexpected error updating role/products for user {}: {}", userId, e.getMessage(), e);
            throw new UserManagementException("Failed to update user access profile.", e);
        }
    }

    /**
     * Executes a user-initiated password change request.
     * This method delegates the credential update directly to Keycloak.
     *
     * @param command A DTO containing the user ID and the new password string.
     * @throws UserNotFoundException If the user ID in the command is invalid.
     * @throws KeycloakInteractionException If the Keycloak API rejects the password update (e.g., policy violation or network error).
     */
    @Transactional
    @Override
    public void changePassword(ChangePasswordCommand command) {
        log.debug("Processing password change request for user ID: {}", command.userId());
        try {
            User user = getUserById(command.userId());
            keycloakService.updateUserPassword(user.getKeycloakId(), command.newPassword());
            log.info("Password successfully changed via Keycloak for user ID: {}", command.userId());
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to execute password change for user ID: {}. Error: {}", command.userId(), e.getMessage(), e);
            throw new KeycloakInteractionException("Failed to update credentials in Identity Provider.", e);
        }
    }

    /**
     * Executes an administrative password reset.
     * Functionally similar to changePassword, but intended for admin flows or recovery processes.
     *
     * @param command A DTO containing the user ID and the temporary/new password string.
     * @throws UserNotFoundException If the user ID in the command is invalid.
     * @throws KeycloakInteractionException If the Keycloak API fails to process the reset request.
     */
    @Transactional
    @Override
    public void resetPassword(ResetPasswordCommand command) {
        log.debug("Processing administrative password reset for user ID: {}", command.userId());
        try {
            User user = getUserById(command.userId());
            keycloakService.updateUserPassword(user.getKeycloakId(), command.newPassword());
            log.info("Administrative password reset completed successfully for user ID: {}", command.userId());
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to execute administrative password reset for user ID: {}. Error: {}", command.userId(), e.getMessage(), e);
            throw new KeycloakInteractionException("Failed to reset credentials in Identity Provider.", e);
        }
    }

    /**
     * Permanently deletes a user from the system.
     * This involves a distributed transaction: first removing the identity from Keycloak,
     * then purging the local database record.
     *
     * @param userId The UUID of the user to be purged.
     * @throws UserNotFoundException If the user ID does not exist locally.
     * @throws KeycloakInteractionException If Keycloak fails to delete the user, leaving the system in a potentially inconsistent state.
     * @throws UserManagementException If the local database deletion fails after Keycloak deletion.
     */
    @Transactional
    @Override
    public void deleteUser(UUID userId) {
        log.debug("Initiating permanent deletion sequence for user ID: {}", userId);
        
        User user = getUserById(userId);

        // Step 1: Remove from Identity Provider
        try {
            log.debug("Requesting user deletion from Keycloak for external ID: {}", user.getKeycloakId());
            keycloakService.deleteUser(user.getKeycloakId());
        } catch (Exception e) {
             log.error("Failed to delete user {} from Keycloak. Halting deletion process. Error: {}", userId, e.getMessage(), e);
             throw new KeycloakInteractionException("Failed to remove user identity from external provider. Aborting deletion.", e);
        }

        // Step 2: Remove from local persistence
        try {
            log.debug("Removing user record from local database for ID: {}", userId);
            userRepository.delete(user);
            log.info("User deletion sequence completed successfully for user ID: {}", userId);
        } catch (Exception e) {
            log.error("CRITICAL: User {} was deleted from Keycloak, but local database deletion failed. System is in an inconsistent state.", userId, e);
            throw new UserManagementException("Local record deletion failed after removing identity provider record.", e);
        }
    }
}
