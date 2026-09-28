package com.guardianservices.userauthentication.account.repository;

import com.guardianservices.userauthentication.account.User;
import com.guardianservices.userauthentication.account.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailNormalized(String emailNormalized);

    Optional<User> findByEmailNormalizedAndStatus(String emailNormalized, UserStatus status);

    boolean existsByEmailNormalized(String emailNormalized);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.status = :status")
    Optional<User> findActiveById(@Param("id") UUID id, @Param("status") UserStatus status);
}