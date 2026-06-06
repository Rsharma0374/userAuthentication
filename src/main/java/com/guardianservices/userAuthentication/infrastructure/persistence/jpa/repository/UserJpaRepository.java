package com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository;

import com.guardianservices.userAuthentication.domain.model.User;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.ibm.asyncutil.util.Either;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entity
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    /**
     * Finds user by username
     *
     * @param username username to search
     * @return optional containing user if found
     */
    Optional<UserJpaEntity> findByUsername(String username);

    /**
     * Finds user by email
     *
     * @param email email to search
     * @return optional containing user if found
     */
    Optional<UserJpaEntity> findByEmail(String email);

    /**
     * Finds user by username or email
     *
     * @param username username to search
     * @param email email to search
     * @return optional containing user if found
     */
    Optional<UserJpaEntity> findByUsernameOrEmail(String username, String email);

    /**
     * Finds user by Keycloak ID
     *
     * @param keycloakId Keycloak user ID
     * @return optional containing user if found
     */
    Optional<UserJpaEntity> findByKeycloakId(String keycloakId);

    /**
     * Finds a user by username and product.
     * Uses a join on the product collection. Handles the case where the product might be null (for SUPER_ADMINs).
     */
    @Query("SELECT DISTINCT u FROM UserJpaEntity u WHERE LOWER(u.username) = :username AND LOWER(u.product) = :product")
    Optional<UserJpaEntity> findByUsernameAndProduct(@Param("username") String username, @Param("product") String product);

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
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserJpaEntity u WHERE LOWER(u.username) = :username AND LOWER(u.product) = :product")
    boolean existsByUsernameAndProduct(@Param("username") String username, @Param("product") String product);

    Page<UserJpaEntity> findAllUsersByProduct(String product, Pageable pageable);
}
