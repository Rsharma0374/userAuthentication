package com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository;

import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.EmailTemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplateJpaEntity, UUID> {
    
    Optional<EmailTemplateJpaEntity> findByTemplateNameAndProductAndIsActiveTrue(String templateName, String product);
    
    // Fallback if no product-specific template is found
    Optional<EmailTemplateJpaEntity> findByTemplateNameAndProductIsNullAndIsActiveTrue(String templateName);
}
