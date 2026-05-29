package com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository;

import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    /**
     * Finds role by name
     *
     * @param name role name
     * @return optional containing role if found
     */
    Optional<ProductJpaEntity> findByName(String name);

    /**
     * Checks if role exists by name
     *
     * @param name role name
     * @return true if exists
     */
    boolean existsByName(String name);
}
