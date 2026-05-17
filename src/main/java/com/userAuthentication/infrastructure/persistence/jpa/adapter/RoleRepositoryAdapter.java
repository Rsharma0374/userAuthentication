package com.userAuthentication.infrastructure.persistence.jpa.adapter;

import com.userAuthentication.domain.model.Role;
import com.userAuthentication.domain.port.out.RoleRepository;
import com.userAuthentication.infrastructure.persistence.jpa.entity.RoleJpaEntity;
import com.userAuthentication.infrastructure.persistence.jpa.repository.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing RoleRepository port using JPA
 */
@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository jpaRepository;

    @Override
    public Role save(Role role) {
        RoleJpaEntity entity = toJpaEntity(role);
        RoleJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomainEntity);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(this::toDomainEntity);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Role role) {
        jpaRepository.deleteById(role.getId());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    /**
     * Converts domain Role to JPA entity
     */
    private RoleJpaEntity toJpaEntity(Role role) {
        return RoleJpaEntity.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    /**
     * Converts JPA entity to domain Role
     */
    private Role toDomainEntity(RoleJpaEntity entity) {
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
