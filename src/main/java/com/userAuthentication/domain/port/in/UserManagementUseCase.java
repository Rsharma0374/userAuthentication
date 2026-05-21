package com.userAuthentication.domain.port.in;

import com.userAuthentication.application.command.ChangePasswordCommand;
import com.userAuthentication.application.command.ResetPasswordCommand;
import com.userAuthentication.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * User management use case interface
 * Defines user CRUD operations
 */
public interface UserManagementUseCase {

    /**
     * Retrieves user by ID
     *
     * @param userId user ID
     * @return user entity
     */
    User getUserById(UUID userId);

    /**
     * Retrieves user by username
     *
     * @param username username
     * @return user entity
     */
    User getUserByUsername(String username);

    /**
     * Retrieves all users
     *
     * @return list of users
     */
    List<User> getAllUsers();

    /**
     * Retrieves paginated users
     *
     * @param pageable pagination info
     * @return page of users
     */
    Page<User> getUsers(Pageable pageable);

    /**
     * Updates user profile
     *
     * @param userId user ID
     * @param user updated user data
     * @return updated user
     */
    User updateUser(UUID userId, User user);

    /**
     * Changes user password
     *
     * @param command change password command
     */
    void changePassword(ChangePasswordCommand command);

    /**
     * Resets user password
     *
     * @param command reset password command
     */
    void resetPassword(ResetPasswordCommand command);

    /**
     * Deletes user
     *
     * @param userId user ID
     */
    void deleteUser(UUID userId);
}
