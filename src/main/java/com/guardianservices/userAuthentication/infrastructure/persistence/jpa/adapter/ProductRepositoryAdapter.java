package com.guardianservices.userAuthentication.infrastructure.persistence.jpa.adapter;


import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.domain.port.out.ProductRepository;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter implementing ProductRepository port using JPA
 */
@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = toJpaEntity(product);
        ProductJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomainEntity(savedEntity);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * Converts domain Product to JPA entity
     */
    private ProductJpaEntity toJpaEntity(Product product) {
        return ProductJpaEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Converts JPA entity to domain Product
     */
    private Product toDomainEntity(ProductJpaEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
