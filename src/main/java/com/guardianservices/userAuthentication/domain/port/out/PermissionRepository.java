package com.guardianservices.userAuthentication.domain.port.out;

import com.guardianservices.userAuthentication.domain.model.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Permission entity
 * Defines persistence operations for permissions
 */
public interface PermissionRepository {

    /**
     * Saves a permission to the database
     *
     * @param permission permission to save
     * @return saved permission
     */
    Permission save(Permission permission);

    /**
     * Finds permission by ID
     *
     * @param id permission ID
     * @return optional containing permission if found
     */
    Optional<Permission> findById(UUID id);

    /**
     * Finds permission by name
     *
     * @param name permission name
     * @return optional containing permission if found
     */
    Optional<Permission> findByName(String name);

    /**
     * Retrieves all permissions
     *
     * @return list of all permissions
     */
    List<Permission> findAll();

    /**
     * Finds permissions by resource and action
     *
     * @param resource resource name
     * @param action action name
     * @return list of matching permissions
     */
    List<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Deletes a permission
     *
     * @param permission permission to delete
     */
    void delete(Permission permission);
}
