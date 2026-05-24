package com.guardianservices.userAuthentication.infrastructure.persistence.jpa.adapter;

import com.guardianservices.userAuthentication.domain.model.Permission;
import com.guardianservices.userAuthentication.domain.port.out.PermissionRepository;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.PermissionJpaEntity;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository.PermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing PermissionRepository port using JPA
 */
@Component
@RequiredArgsConstructor
public class PermissionRepositoryAdapter implements PermissionRepository {

    private final PermissionJpaRepository jpaRepository;

    @Override
    public Permission save(Permission permission) {
        PermissionJpaEntity entity = toJpaEntity(permission);
        PermissionJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Permission> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomainEntity);
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(this::toDomainEntity);
    }

    @Override
    public List<Permission> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> findByResourceAndAction(String resource, String action) {
        return jpaRepository.findByResourceAndAction(resource, action).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Permission permission) {
        jpaRepository.deleteById(permission.getId());
    }

    /**
     * Converts domain Permission to JPA entity
     */
    private PermissionJpaEntity toJpaEntity(Permission permission) {
        return PermissionJpaEntity.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .action(permission.getAction())
                .createdAt(permission.getCreatedAt())
                .build();
    }

    /**
     * Converts JPA entity to domain Permission
     */
    private Permission toDomainEntity(PermissionJpaEntity entity) {
        return Permission.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .resource(entity.getResource())
                .action(entity.getAction())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
