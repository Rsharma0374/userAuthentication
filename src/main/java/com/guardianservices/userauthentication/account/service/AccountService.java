package com.guardianservices.userauthentication.account.service;

import com.guardianservices.userauthentication.account.ActionToken;
import com.guardianservices.userauthentication.account.ActionTokenPurpose;
import com.guardianservices.userauthentication.account.User;
import com.guardianservices.userauthentication.account.UserStatus;
import com.guardianservices.userauthentication.account.repository.ActionTokenRepository;
import com.guardianservices.userauthentication.account.repository.UserRepository;
import com.guardianservices.userauthentication.common.exception.ConflictException;
import com.guardianservices.userauthentication.common.exception.NotFoundException;
import com.guardianservices.userauthentication.common.exception.UnauthorizedException;
import com.guardianservices.userauthentication.common.exception.ValidationException;
import com.guardianservices.userauthentication.common.util.Clock;
import com.guardianservices.userauthentication.common.util.EmailNormalizer;
import com.guardianservices.userauthentication.common.util.SecureTokenGenerator;
import com.guardianservices.userauthentication.common.util.TokenHasher;
import com.guardianservices.userauthentication.notification.OutboxEvent;
import com.guardianservices.userauthentication.notification.repository.OutboxEventRepository;
import com.guardianservices.userauthentication.platform.config.AuthProperties;
import com.guardianservices.userauthentication.platform.config.NotificationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final ActionTokenRepository actionTokenRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final EmailNormalizer emailNormalizer;
    private final SecureTokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;
    private final Clock clock;
    private final AuthProperties authProperties;
    private final NotificationProperties notificationProperties;

    @Transactional
    public User register(String email, String passwordHash) {
        return registerInternal(email, passwordHash).user();
    }

    @Transactional
    public RegistrationResult registerWithOutcome(String email, String passwordHash) {
        return registerInternal(email, passwordHash);
    }

    private RegistrationResult registerInternal(String email, String passwordHash) {
        String normalizedEmail = emailNormalizer.normalize(email);
        
        // Check if user already exists with this email (any status except DELETED)
        Optional<User> existingUser = userRepository.findByEmailNormalized(normalizedEmail);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getStatus() != UserStatus.DELETED) {
                // Return generic accepted response - don't reveal if account exists
                // But we still need to send verification email if PENDING_VERIFICATION
                if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
                    sendVerificationEmail(user);
                }
                return new RegistrationResult(user, true);
            }
            // If user is DELETED, we can proceed with new registration
        }

        // Create new user
        User user = new User();
        user.setEmailOriginal(email);
        user.setEmailNormalized(normalizedEmail);
        user.setPasswordHash(passwordHash);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setCredentialsChangedAt(clock.now());
        user.setCreatedAt(clock.now());
        user.setUpdatedAt(clock.now());

        user = userRepository.save(user);

        // Create verification token and send email
        sendVerificationEmail(user);

        log.info("User registered: {}", user.getId());
        return new RegistrationResult(user, false);
    }

    public record RegistrationResult(User user, boolean existingAccount) {}

    private void sendVerificationEmail(User user) {
        String token = tokenGenerator.generateToken();
        byte[] tokenHash = tokenHasher.hash(token);

        ActionToken actionToken = new ActionToken();
        actionToken.setUser(user);
        actionToken.setPurpose(ActionTokenPurpose.EMAIL_VERIFICATION);
        actionToken.setTokenHash(tokenHash);
        actionToken.setExpiresAt(clock.now().plus(authProperties.getActionToken().getVerificationTtl()));
        actionToken.setCreatedAt(clock.now());

        actionTokenRepository.save(actionToken);

        // Create outbox event for email delivery
        String verificationUrl = buildVerificationUrl(token);
        OutboxEvent event = createEmailEvent(
            user.getId(),
            "EMAIL_VERIFICATION",
            Map.of(
                "email", user.getEmailOriginal(),
                "verificationUrl", verificationUrl,
                "expiresIn", authProperties.getActionToken().getVerificationTtl().toString()
            )
        );

        outboxEventRepository.save(event);
    }

    @Transactional
    public void verifyEmail(String token) {
        byte[] tokenHash = tokenHasher.hash(token);

        ActionToken actionToken = actionTokenRepository.findActiveByTokenHashForUpdate(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Invalid or expired verification token"));

        if (actionToken.getPurpose() != ActionTokenPurpose.EMAIL_VERIFICATION) {
            throw new UnauthorizedException("Invalid token purpose");
        }

        User user = actionToken.getUser();
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new ConflictException("Account is not pending verification");
        }

        // Mark token as consumed
        actionToken.setConsumedAt(clock.now());
        actionTokenRepository.save(actionToken);

        // Activate user
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerifiedAt(clock.now());
        user.setUpdatedAt(clock.now());
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getId());
    }

    @Transactional
    public void resendVerification(String email) {
        String normalizedEmail = emailNormalizer.normalize(email);
        
        Optional<User> userOpt = userRepository.findByEmailNormalizedAndStatus(normalizedEmail, UserStatus.PENDING_VERIFICATION);
        if (userOpt.isEmpty()) {
            // Generic response - don't reveal if account exists
            return;
        }

        User user = userOpt.get();
        
        // Delete any existing verification tokens for this user
        actionTokenRepository.deleteExpired(clock.now());
        
        sendVerificationEmail(user);
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        String normalizedEmail = emailNormalizer.normalize(email);
        
        Optional<User> userOpt = userRepository.findByEmailNormalized(normalizedEmail);
        if (userOpt.isEmpty() || userOpt.get().getStatus() == UserStatus.DELETED) {
            // Generic response - don't reveal if account exists
            return;
        }

        User user = userOpt.get();
        
        String token = tokenGenerator.generateToken();
        byte[] tokenHash = tokenHasher.hash(token);

        ActionToken actionToken = new ActionToken();
        actionToken.setUser(user);
        actionToken.setPurpose(ActionTokenPurpose.PASSWORD_RESET);
        actionToken.setTokenHash(tokenHash);
        actionToken.setExpiresAt(clock.now().plus(authProperties.getActionToken().getPasswordResetTtl()));
        actionToken.setCreatedAt(clock.now());

        actionTokenRepository.save(actionToken);

        String resetUrl = buildPasswordResetUrl(token);
        OutboxEvent event = createEmailEvent(
            user.getId(),
            "PASSWORD_RESET",
            Map.of(
                "email", user.getEmailOriginal(),
                "resetUrl", resetUrl,
                "expiresIn", authProperties.getActionToken().getPasswordResetTtl().toString()
            )
        );

        outboxEventRepository.save(event);
    }

    @Transactional
    public User resetPassword(String token, String newPasswordHash) {
        byte[] tokenHash = tokenHasher.hash(token);

        ActionToken actionToken = actionTokenRepository.findActiveByTokenHashForUpdate(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Invalid or expired reset token"));

        if (actionToken.getPurpose() != ActionTokenPurpose.PASSWORD_RESET) {
            throw new UnauthorizedException("Invalid token purpose");
        }

        User user = actionToken.getUser();
        
        // Mark token as consumed
        actionToken.setConsumedAt(clock.now());
        actionTokenRepository.save(actionToken);

        // Update password
        user.setPasswordHash(newPasswordHash);
        user.setCredentialsChangedAt(clock.now());
        user.setUpdatedAt(clock.now());
        userRepository.save(user);

        // Revoke all refresh sessions (handled by session service)
        // Send security notification
        OutboxEvent event = createEmailEvent(
            user.getId(),
            "PASSWORD_CHANGED",
            Map.of("email", user.getEmailOriginal())
        );
        outboxEventRepository.save(event);

        log.info("Password reset for user: {}", user.getId());
        return user;
    }

    @Transactional
    public void initiateEmailChange(User user, String newEmail) {
        String normalizedNewEmail = emailNormalizer.normalize(newEmail);
        
        if (userRepository.existsByEmailNormalized(normalizedNewEmail)) {
            throw new ConflictException("Email already in use");
        }

        if (normalizedNewEmail.equals(user.getEmailNormalized())) {
            throw new ValidationException("New email must be different from current email", Map.of("email", "Email unchanged"));
        }

        String token = tokenGenerator.generateToken();
        byte[] tokenHash = tokenHasher.hash(token);

        ActionToken actionToken = new ActionToken();
        actionToken.setUser(user);
        actionToken.setPurpose(ActionTokenPurpose.EMAIL_CHANGE);
        actionToken.setTokenHash(tokenHash);
        actionToken.setExpiresAt(clock.now().plus(authProperties.getActionToken().getEmailChangeTtl()));
        actionToken.setCreatedAt(clock.now());
        actionToken.setMetadata("{\"newEmail\":\"" + normalizedNewEmail + "\",\"originalEmail\":\"" + user.getEmailOriginal() + "\"}");

        actionTokenRepository.save(actionToken);

        String changeUrl = buildEmailChangeUrl(token);
        OutboxEvent event = createEmailEvent(
            user.getId(),
            "EMAIL_CHANGE",
            Map.of(
                "currentEmail", user.getEmailOriginal(),
                "newEmail", newEmail,
                "changeUrl", changeUrl,
                "expiresIn", authProperties.getActionToken().getEmailChangeTtl().toString()
            )
        );

        outboxEventRepository.save(event);
    }

    @Transactional
    public User confirmEmailChange(String token) {
        byte[] tokenHash = tokenHasher.hash(token);

        ActionToken actionToken = actionTokenRepository.findActiveByTokenHashForUpdate(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Invalid or expired email change token"));

        if (actionToken.getPurpose() != ActionTokenPurpose.EMAIL_CHANGE) {
            throw new UnauthorizedException("Invalid token purpose");
        }

        User user = actionToken.getUser();
        
        // Parse new email from metadata
        String newEmail = parseNewEmailFromMetadata(actionToken.getMetadata());
        if (newEmail == null) {
            throw new IllegalStateException("Invalid email change token metadata");
        }

        // Check if email is still available
        if (userRepository.existsByEmailNormalized(newEmail)) {
            throw new ConflictException("Email already in use");
        }

        // Mark token as consumed
        actionToken.setConsumedAt(clock.now());
        actionTokenRepository.save(actionToken);

        // Update email
        user.setEmailOriginal(newEmail); // We'd need to preserve original display format
        user.setEmailNormalized(newEmail);
        user.setEmailVerifiedAt(clock.now());
        user.setCredentialsChangedAt(clock.now());
        user.setUpdatedAt(clock.now());
        userRepository.save(user);

        // Send notification to old email
        OutboxEvent event = createEmailEvent(
            user.getId(),
            "EMAIL_CHANGED",
            Map.of("newEmail", newEmail)
        );
        outboxEventRepository.save(event);

        log.info("Email changed for user: {}", user.getId());
        return user;
    }

    private String parseNewEmailFromMetadata(String metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            // Simple JSON parsing - in production use Jackson
            int start = metadata.indexOf("\"newEmail\":\"");
            if (start == -1) return null;
            start += 12;
            int end = metadata.indexOf("\"", start);
            return metadata.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildVerificationUrl(String token) {
        return notificationProperties.getEmail().getBaseUrl() + 
               notificationProperties.getEmail().getVerificationPath() + 
               "?token=" + token;
    }

    private String buildPasswordResetUrl(String token) {
        return notificationProperties.getEmail().getBaseUrl() + 
               notificationProperties.getEmail().getPasswordResetPath() + 
               "?token=" + token;
    }

    private String buildEmailChangeUrl(String token) {
        return notificationProperties.getEmail().getBaseUrl() + 
               notificationProperties.getEmail().getEmailChangePath() + 
               "?token=" + token;
    }

    private OutboxEvent createEmailEvent(UUID userId, String type, Map<String, Object> payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateId(userId);
        event.setType(type);
        event.setSchemaVersion(1);
        event.setAvailableAt(clock.now());
        // In production, payload would be encrypted
        try {
            event.setPayload(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize payload", e);
        }
        return event;
    }
}