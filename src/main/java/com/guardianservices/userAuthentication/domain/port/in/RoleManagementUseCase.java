package com.guardianservices.userAuthentication.domain.port.in;

import com.guardianservices.userAuthentication.application.command.AssignRoleCommand;
import com.guardianservices.userAuthentication.application.command.CreateRoleCommand;
import com.guardianservices.userAuthentication.application.command.RemoveRoleCommand;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Role management use case interface
 * Defines role-related operations
 */
public interface RoleManagementUseCase {

    /**
     * Creates a new role
     *
     * @param command create role command
     */
    void createRole(CreateRoleCommand command);

    /**
     * Assigns role to user
     *
     * @param command assign role command
     */
    void assignRoleToUser(AssignRoleCommand command);

    /**
     * Removes role from user
     *
     * @param command remove role command
     */
    void removeRoleFromUser(RemoveRoleCommand command);

    /**
     * Retrieves all roles
     *
     * @return list of role names
     */
    List<String> getAllRoles();

    /**
     * Retrieves user's roles
     *
     * @param userId user ID
     * @return set of role names
     */
    Set<String> getUserRoles(UUID userId);

    /**
     * Checks if user has role
     *
     * @param userId user ID
     * @param roleName role name
     * @return true if user has role
     */
    boolean hasRole(UUID userId, String roleName);
}
