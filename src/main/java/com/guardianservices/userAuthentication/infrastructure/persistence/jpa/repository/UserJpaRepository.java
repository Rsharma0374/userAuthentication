package com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository;

import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Checks if user exists by username
     */
    boolean existsByUsername(String username);

    /**
     * Checks if user exists by email
     */
    boolean existsByEmail(String email);
}
