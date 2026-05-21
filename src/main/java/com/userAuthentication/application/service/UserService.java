package com.userAuthentication.application.service;

import com.userAuthentication.application.command.ChangePasswordCommand;
import com.userAuthentication.application.command.ResetPasswordCommand;
import com.userAuthentication.domain.model.User;
import com.userAuthentication.domain.port.in.UserManagementUseCase;
import com.userAuthentication.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for user management operations
 * Synchronizes user data between local database and Keycloak
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserManagementUseCase {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    /**
     * Retrieves user by ID
     *
     * @param userId user UUID
     * @return user entity
     */
    @Transactional(readOnly = true)
    @Override
    public User getUserById(UUID userId) {
        log.debug("Retrieving user by ID: {}", userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    /**
     * Retrieves user by username
     *
     * @param username username
     * @return user entity
     */
    @Transactional(readOnly = true)
    @Override
    public User getUserByUsername(String username) {
        log.debug("Retrieving user by username: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    /**
     * Retrieves all users
     *
     * @return list of all users
     */
    @Transactional(readOnly = true)
    @Override
    public List<User> getAllUsers() {
        log.debug("Retrieving all users");
        return userRepository.findAll();
    }

    /**
     * Retrieves paginated users
     *
     * @param pageable pagination info
     * @return page of users
     */
    @Transactional(readOnly = true)
    @Override
    public Page<User> getUsers(Pageable pageable) {
        log.debug("Retrieving users with pagination: {}", pageable);
        return userRepository.findAll(pageable);
    }

    /**
     * Updates user profile information
     * Updates both local database and Keycloak
     *
     * @param userId user ID
     * @param updatedUser user with updated information
     * @return updated user
     */
    @Transactional
    @Override
    public User updateUser(UUID userId, User updatedUser) {
        log.debug("Updating user: {}", userId);

        User existingUser = getUserById(userId);

        // Update local database
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());

        userRepository.save(existingUser);

        // TODO: Update user in Keycloak if needed

        log.info("User updated successfully: {}", userId);
        return existingUser;
    }

    /**
     * Changes user password
     * Updates password in Keycloak
     *
     * @param command change password command
     */
    @Transactional
    @Override
    public void changePassword(ChangePasswordCommand command) {
        log.debug("Changing password for user: {}", command.userId());

        User user = getUserById(command.userId());

        // Update password in Keycloak
        keycloakService.updateUserPassword(user.getKeycloakId(), command.newPassword());

        log.info("Password changed successfully for user: {}", command.userId());
    }

    /**
     * Resets user password
     *
     * @param command reset password command
     */
    @Transactional
    @Override
    public void resetPassword(ResetPasswordCommand command) {
        log.debug("Resetting password for user: {}", command.userId());

        User user = getUserById(command.userId());

        keycloakService.updateUserPassword(user.getKeycloakId(), command.newPassword());

        log.info("Password reset successfully for user: {}", command.userId());
    }

    /**
     * Deletes user
     * Removes user from both local database and Keycloak
     *
     * @param userId user ID to delete
     */
    @Transactional
    @Override
    public void deleteUser(UUID userId) {
        log.debug("Deleting user: {}", userId);

        User user = getUserById(userId);

        // Delete from Keycloak
        keycloakService.deleteUser(user.getKeycloakId());

        // Delete from local database
        userRepository.delete(user);

        log.info("User deleted successfully: {}", userId);
    }
}
