package com.guardianservices.userAuthentication.domain.port.out;

import com.guardianservices.userAuthentication.domain.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Role entity
 * Defines persistence operations for roles
 */
public interface RoleRepository {

    /**
     * Saves a role to the database
     *
     * @param role role to save
     * @return saved role
     */
    Role save(Role role);

    /**
     * Finds role by ID
     *
     * @param id role ID
     * @return optional containing role if found
     */
    Optional<Role> findById(UUID id);

    /**
     * Finds role by name
     *
     * @param name role name
     * @return optional containing role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Retrieves all roles
     *
     * @return list of all roles
     */
    List<Role> findAll();

    /**
     * Deletes a role
     *
     * @param role role to delete
     */
    void delete(Role role);

    /**
     * Checks if role exists by name
     *
     * @param name role name
     * @return true if role exists
     */
    boolean existsByName(String name);
}
