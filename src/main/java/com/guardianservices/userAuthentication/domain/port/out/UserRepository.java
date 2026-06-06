package com.guardianservices.userAuthentication.domain.port.out;

import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity
 * Defines persistence operations for users
 */
public interface UserRepository {

    /**
     * Saves a user to the database
     *
     * @param user user to save
     * @return saved user
     */
    User save(User user);

    /**
     * Finds user by ID
     *
     * @param id user ID
     * @return optional containing user if found
     */
    Optional<User> findById(UUID id);

    /**
     * Finds user by username
     *
     * @param username username to search
     * @return optional containing user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds user by email
     *
     * @param email email to search
     * @return optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds user by username or email
     *
     * @param username username to search
     * @param email email to search
     * @return optional containing user if found
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Finds a user by their username and associated product.
     *
     * @param username The username to search for.
     * @param product The product the user must be associated with.
     * @return An optional containing the user if found.
     */
    Optional<User> findByUsernameAndProduct(String username, String product);

    /**
     * Finds user by Keycloak ID
     *
     * @param keycloakId Keycloak user ID
     * @return optional containing user if found
     */
    Optional<User> findByKeycloakId(String keycloakId);

    /**
     * Retrieves all users
     *
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Retrieves paginated users
     *
     * @param pageable pagination information
     * @return page of users
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Deletes a user
     *
     * @param user user to delete
     */
    void delete(User user);

    /**
     * Checks if user exists by username
     *
     * @param username username to check
     * @return true if user exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks if user exists by email
     *
     * @param email email to check
     * @return true if user exists
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists with the given username and product association.
     *
     * @param username The username.
     * @param product The product.
     * @return true if such a user exists, false otherwise.
     */
    boolean existsByUsernameAndProduct(String username, String product);

    Page<User> findByProduct(String product, Pageable pageable);
}
