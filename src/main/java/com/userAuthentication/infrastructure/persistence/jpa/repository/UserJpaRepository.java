package com.userAuthentication.infrastructure.persistence.jpa.repository;

import com.userAuthentication.domain.model.Product;
import com.userAuthentication.infrastructure.persistence.jpa.entity.UserJpaEntity;
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
     * Checks if user exists by username and product
     * Handle null product for admin users
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserJpaEntity u WHERE u.username = :username AND (u.product = :product OR (u.product IS NULL AND :product IS NULL))")
    boolean existsByUsernameAndProduct(@Param("username") String username, @Param("product") Product product);

    /**
     * Checks if user exists by email and product
     * Handle null product for admin users
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserJpaEntity u WHERE u.email = :email AND (u.product = :product OR (u.product IS NULL AND :product IS NULL))")
    boolean existsByEmailAndProduct(@Param("email") String email, @Param("product") Product product);
}
