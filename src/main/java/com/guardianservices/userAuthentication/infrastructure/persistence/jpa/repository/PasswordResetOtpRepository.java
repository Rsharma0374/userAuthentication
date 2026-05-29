package com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository;

import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.PasswordResetOtpJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtpJpaEntity, UUID> {
    Optional<PasswordResetOtpJpaEntity> findByRequestId(UUID requestId);
}
