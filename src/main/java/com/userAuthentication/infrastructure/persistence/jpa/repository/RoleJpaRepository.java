package com.userAuthentication.infrastructure.persistence.jpa.repository;

import com.userAuthentication.infrastructure.persistence.jpa.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Role entity
 */
@Repository
public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, UUID> {

    /**
     * Finds role by name
     *
     * @param name role name
     * @return optional containing role if found
     */
    Optional<RoleJpaEntity> findByName(String name);

    /**
     * Checks if role exists by name
     *
     * @param name role name
     * @return true if exists
     */
    boolean existsByName(String name);
}
