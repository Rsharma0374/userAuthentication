package com.guardianservices.userAuthentication.domain.port.in;

import com.guardianservices.userAuthentication.application.command.ChangePasswordCommand;
import com.guardianservices.userAuthentication.application.command.ResetPasswordCommand;
import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.domain.model.User;
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
     * Updates user role and products
     *
     * @param userId user ID
     * @param newRole new role to assign
     * @param products new products list to assign
     * @return updated user
     */
    User updateUserRoleAndProducts(UUID userId, String newRole, List<Product> products);

    /**
     * Changes user password
     *
     * @param command change password command
     */
    void changePassword(ChangePasswordCommand command, String ipAddress);

//    /**
//     * Resets user password
//     *
//     * @param command reset password command
//     */
//    void resetPassword(ResetPasswordCommand command);

    /**
     * Deletes user
     *
     * @param userId user ID
     */
    void deleteUser(UUID userId);
}
