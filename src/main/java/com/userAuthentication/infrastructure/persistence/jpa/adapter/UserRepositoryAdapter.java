package com.userAuthentication.infrastructure.persistence.jpa.adapter;

import com.userAuthentication.domain.model.User;
import com.userAuthentication.domain.port.out.UserRepository;
import com.userAuthentication.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.userAuthentication.infrastructure.persistence.jpa.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing UserRepository port using JPA
 * Converts between domain User and JPA entity
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    /**
     * Saves user to database
     *
     * @param user domain user entity
     * @return saved domain user
     */
    @Override
    public User save(User user) {
        UserJpaEntity entity = toJpaEntity(user);
        UserJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomainEntity(savedEntity);
    }

    /**
     * Finds user by ID
     *
     * @param id user ID
     * @return optional containing domain user
     */
    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomainEntity);
    }

    /**
     * Finds user by username
     *
     * @param username username
     * @return optional containing domain user
     */
    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(this::toDomainEntity);
    }

    /**
     * Finds user by email
     *
     * @param email email
     * @return optional containing domain user
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(this::toDomainEntity);
    }

    /**
     * Finds user by username or email
     *
     * @param username username
     * @param email email
     * @return optional containing domain user
     */
    @Override
    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return jpaRepository.findByUsernameOrEmail(username, email)
                .map(this::toDomainEntity);
    }

    /**
     * Finds user by Keycloak ID
     *
     * @param keycloakId Keycloak user ID
     * @return optional containing domain user
     */
    @Override
    public Optional<User> findByKeycloakId(String keycloakId) {
        return jpaRepository.findByKeycloakId(keycloakId)
                .map(this::toDomainEntity);
    }

    /**
     * Retrieves all users
     *
     * @return list of domain users
     */
    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * Deletes user
     *
     * @param user user to delete
     */
    @Override
    public void delete(User user) {
        jpaRepository.deleteById(user.getId());
    }

    /**
     * Checks if user exists by username
     *
     * @param username username
     * @return true if exists
     */
    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    /**
     * Checks if user exists by email
     *
     * @param email email
     * @return true if exists
     */
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    /**
     * Checks if user exists by username or email
     *
     * @param username username
     * @param email email
     * @return true if exists
     */
    @Override
    public boolean existsByUsernameOrEmail(String username, String email) {
        return jpaRepository.existsByUsernameOrEmail(username, email);
    }

    /**
     * Converts domain User to JPA entity
     *
     * @param user domain user
     * @return JPA entity
     */
    private UserJpaEntity toJpaEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .mfaEnabled(user.getMfaEnabled())
                .mfaSecret(user.getMfaSecret())
                .lastLogin(user.getLastLogin())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Converts JPA entity to domain User
     *
     * @param entity JPA entity
     * @return domain user
     */
    private User toDomainEntity(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .keycloakId(entity.getKeycloakId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .enabled(entity.getEnabled())
                .emailVerified(entity.getEmailVerified())
                .mfaEnabled(entity.getMfaEnabled())
                .mfaSecret(entity.getMfaSecret())
                .lastLogin(entity.getLastLogin())
                .roles(entity.getRoles())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
