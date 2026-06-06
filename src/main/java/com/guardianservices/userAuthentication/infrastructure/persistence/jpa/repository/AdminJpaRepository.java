package com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository;

import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.AdminJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminJpaRepository extends JpaRepository<AdminJpaEntity, UUID> {


    /**
     * Finds user by username
     *
     * @param username username to search
     * @return optional containing user if found
     */
    Optional<AdminJpaEntity> findByUsernameIgnoreCase(String username);

    /**
     * Finds user by email
     *
     * @param email email to search
     * @return optional containing user if found
     */
    Optional<AdminJpaEntity> findByEmail(String email);

    /**
     * Finds user by username or email
     *
     * @param username username to search
     * @param email email to search
     * @return optional containing user if found
     */
    Optional<AdminJpaEntity> findByUsernameOrEmail(String username, String email);

    /**
     * Finds user by Keycloak ID
     *
     * @param keycloakId Keycloak user ID
     * @return optional containing user if found
     */
    Optional<AdminJpaEntity> findByKeycloakId(String keycloakId);

    /**
     * Finds a user by username and product.
     * Uses a join on the product collection. Handles the case where the product might be null (for SUPER_ADMINs).
     */
    @Query("""
    SELECT DISTINCT u
    FROM AdminJpaEntity u
    WHERE LOWER(u.username) = LOWER(:username)
    AND (
        (:product IS NULL AND u.product IS NULL)
        OR (
            :product IS NOT NULL
            AND LOWER(u.product) = LOWER(CAST(:product AS string))
        )
    )
""")
    Optional<AdminJpaEntity> findByUsernameAndProduct(@Param("username") String username, @Param("product") String product);

    /**
     * Checks if user exists by username
     */
    boolean existsByUsername(String username);

    /**
     * Checks if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Checks if user exists by username and product.
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM AdminJpaEntity u WHERE LOWER(u.username) = LOWER(:username)AND ((:product IS NULL AND u.product IS NULL)OR LOWER(u.product) = LOWER(:product))")
    boolean existsByUsernameAndProduct(@Param("username") String username, @Param("product") String product);
}
