package com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository;

import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Permission entity
 * Provides CRUD operations and custom queries for permission management
 */
@Repository
public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, UUID> {

    /**
     * Finds permission by exact name match
     *
     * @param name permission name (e.g., "user:read", "admin:access")
     * @return optional containing permission if found
     */
    Optional<PermissionJpaEntity> findByName(String name);

    /**
     * Finds permissions by resource name
     *
     * @param resource resource name (e.g., "user", "role", "document")
     * @return list of permissions for the specified resource
     */
    List<PermissionJpaEntity> findByResource(String resource);

    /**
     * Finds permissions by action
     *
     * @param action action name (e.g., "read", "write", "delete")
     * @return list of permissions with the specified action
     */
    List<PermissionJpaEntity> findByAction(String action);

    /**
     * Finds permission by resource and action combination
     *
     * @param resource resource name
     * @param action action name
     * @return optional containing permission if found
     */
    Optional<PermissionJpaEntity> findByResourceAndAction(String resource, String action);

    /**
     * Finds permissions by resource name (case-insensitive)
     *
     * @param resource resource name
     * @return list of permissions for the specified resource
     */
    @Query("SELECT p FROM PermissionJpaEntity p WHERE LOWER(p.resource) = LOWER(:resource)")
    List<PermissionJpaEntity> findByResourceIgnoreCase(@Param("resource") String resource);

    /**
     * Finds permissions by action (case-insensitive)
     *
     * @param action action name
     * @return list of permissions with the specified action
     */
    @Query("SELECT p FROM PermissionJpaEntity p WHERE LOWER(p.action) = LOWER(:action)")
    List<PermissionJpaEntity> findByActionIgnoreCase(@Param("action") String action);

    /**
     * Finds permissions by name containing the given string
     *
     * @param namePart part of permission name
     * @return list of permissions matching the name pattern
     */
    List<PermissionJpaEntity> findByNameContainingIgnoreCase(String namePart);

    /**
     * Finds permissions by resource and action (case-insensitive)
     *
     * @param resource resource name
     * @param action action name
     * @return optional containing permission if found
     */
    @Query("SELECT p FROM PermissionJpaEntity p WHERE LOWER(p.resource) = LOWER(:resource) AND LOWER(p.action) = LOWER(:action)")
    Optional<PermissionJpaEntity> findByResourceAndActionIgnoreCase(@Param("resource") String resource, @Param("action") String action);

    /**
     * Checks if permission exists by name
     *
     * @param name permission name
     * @return true if permission exists
     */
    boolean existsByName(String name);

    /**
     * Checks if permission exists by resource and action
     *
     * @param resource resource name
     * @param action action name
     * @return true if permission exists
     */
    boolean existsByResourceAndAction(String resource, String action);

    /**
     * Deletes permission by name
     *
     * @param name permission name
     * @return number of deleted records
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PermissionJpaEntity p WHERE p.name = :name")
    int deleteByName(@Param("name") String name);

    /**
     * Deletes all permissions for a specific resource
     *
     * @param resource resource name
     * @return number of deleted records
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PermissionJpaEntity p WHERE p.resource = :resource")
    int deleteByResource(@Param("resource") String resource);

    /**
     * Finds all permissions with pagination and sorting
     *
     * @param resource resource filter (optional)
     * @param action action filter (optional)
     * @return list of permissions
     */
    @Query("SELECT p FROM PermissionJpaEntity p WHERE " +
            "(:resource IS NULL OR p.resource = :resource) AND " +
            "(:action IS NULL OR p.action = :action)")
    List<PermissionJpaEntity> findByResourceAndActionOptional(
            @Param("resource") String resource,
            @Param("action") String action);

    /**
     * Count permissions by resource
     *
     * @param resource resource name
     * @return count of permissions for the resource
     */
    long countByResource(String resource);

    /**
     * Count permissions by action
     *
     * @param action action name
     * @return count of permissions with the action
     */
    long countByAction(String action);

    /**
     * Finds all distinct resources
     *
     * @return list of unique resource names
     */
    @Query("SELECT DISTINCT p.resource FROM PermissionJpaEntity p ORDER BY p.resource")
    List<String> findAllDistinctResources();

    /**
     * Finds all distinct actions
     *
     * @return list of unique action names
     */
    @Query("SELECT DISTINCT p.action FROM PermissionJpaEntity p ORDER BY p.action")
    List<String> findAllDistinctActions();

    /**
     * Finds permissions by multiple resource names
     *
     * @param resources list of resource names
     * @return list of permissions for the specified resources
     */
    List<PermissionJpaEntity> findByResourceIn(List<String> resources);

    /**
     * Finds permissions by multiple action names
     *
     * @param actions list of action names
     * @return list of permissions with the specified actions
     */
    List<PermissionJpaEntity> findByActionIn(List<String> actions);

    /**
     * Finds permissions created after a specific date
     *
     * @param timestamp timestamp to compare
     * @return list of permissions created after the timestamp
     */
    @Query("SELECT p FROM PermissionJpaEntity p WHERE p.createdAt > :timestamp")
    List<PermissionJpaEntity> findCreatedAfter(@Param("timestamp") java.time.LocalDateTime timestamp);

    /**
     * Batch update permission descriptions
     *
     * @param name permission name
     * @param description new description
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE PermissionJpaEntity p SET p.description = :description WHERE p.name = :name")
    int updateDescriptionByName(@Param("name") String name, @Param("description") String description);

    /**
     * Finds permissions with native SQL query (example for complex cases)
     *
     * @param resourcePattern resource name pattern
     * @return list of permissions matching the pattern
     */
    @Query(value = "SELECT * FROM permissions WHERE resource LIKE :resourcePattern", nativeQuery = true)
    List<PermissionJpaEntity> findByResourcePattern(@Param("resourcePattern") String resourcePattern);

    /**
     * Gets all permissions as a map of name to permission (for caching)
     *
     * @return list of all permissions with their names
     */
    @Query("SELECT p FROM PermissionJpaEntity p")
    List<PermissionJpaEntity> findAllForCache();
}
