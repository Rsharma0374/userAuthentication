//package com.guardianservices.userAuthentication.application.service;
//
//import com.guardianservices.userAuthentication.application.command.ChangePasswordCommand;
//import com.guardianservices.userAuthentication.application.exception.KeycloakInteractionException;
//import com.guardianservices.userAuthentication.application.exception.UserManagementException;
//import com.guardianservices.userAuthentication.application.exception.UserNotFoundException;
//import com.guardianservices.userAuthentication.domain.model.Product;
//import com.guardianservices.userAuthentication.domain.model.User;
//import com.guardianservices.userAuthentication.domain.port.in.UserManagementUseCase;
//import com.guardianservices.userAuthentication.infrastructure.messaging.EmailEventPayload;
//import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.EmailTemplateJpaEntity;
//import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.PasswordResetOtpJpaEntity;
//import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.entity.UserJpaEntity;
//import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository.PasswordResetOtpRepository;
//import com.guardianservices.userAuthentication.domain.port.out.UserRepository;
//import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository.UserJpaRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.security.SecureRandom;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.ThreadLocalRandom;
//
///**
// * Service responsible for user management operations.
// * This service acts as an orchestrator between the local database and the external Identity Provider (Keycloak).
// * It ensures that user profiles, roles, and credentials remain synchronized across the ecosystem.
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class UserService implements UserManagementUseCase {
//
//    private final UserRepository userRepository;
//    private final UserJpaRepository userJpaRepository;
//    private final KeycloakService keycloakService;
//    private final PasswordResetOtpRepository passwordResetOtpRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//    private final EmailTemplateService emailTemplateService;
//
//    /**
//     * Initiates the password reset process for a user.
//     * Generates a secure OTP, hashes it, and stores it in the database.
//     * Fetches the appropriate email template, renders it, and publishes a Kafka event.
//     *
//     * @param usernameOrEmail The username or email of the user requesting the reset.
//     * @param product The product scope for the user. Can be null for SUPER_ADMINs.
//     * @return A unique request ID for this password reset attempt.
//     * @throws UserNotFoundException if no user is found with the given identifier for the specified product.
//     */
//    @Transactional
//    public UUID requestPasswordReset(String usernameOrEmail, Product product) {
//        log.debug("Password reset requested for identifier: {} and product: {}", usernameOrEmail, product);
//        try {
//            UserJpaEntity user = userJpaRepository.findByUsernameAndProduct(usernameOrEmail, product)
//                    .orElseThrow(() -> new UserNotFoundException("User not found with identifier: " + usernameOrEmail));
//
//            String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 999999));
//            String otpHash = passwordEncoder.encode(otp);
//            UUID requestId = UUID.randomUUID();
//
//            PasswordResetOtpJpaEntity otpEntity = PasswordResetOtpJpaEntity.builder()
//                    .requestId(requestId)
//                    .user(user)
//                    .otpHash(otpHash)
//                    .expiresAt(LocalDateTime.now().plusMinutes(15))
//                    .used(false)
//                    .createdAt(LocalDateTime.now())
//                    .build();
//
//            passwordResetOtpRepository.save(otpEntity);
//
//            // Fetch and render the email template
//            String templateName = "PASSWORD_RESET_OTP";
//            EmailTemplateJpaEntity templateEntity = emailTemplateService.findActiveTemplate(templateName, product)
//                    .orElseThrow(() -> new UserManagementException("Active email template not found: " + templateName, null));
//
//            Map<String, String> variables = Map.of(
//                    "username", user.getUsername(),
//                    "otp", otp,
//                    "role", user.getRoles().stream().findFirst().orElse(""),
//                    "expiresAt", otpEntity.getExpiresAt().toString()
//            );
//
//            String renderedSubject = emailTemplateService.renderTemplate(templateEntity.getSubject(), variables);
//            String renderedBody = emailTemplateService.renderTemplate(templateEntity.getBody(), variables);
//
//            // Asynchronously send email via Kafka
//            EmailEventPayload payload = EmailEventPayload.builder()
//                    .requestId(requestId)
//                    .recipientEmail(user.getEmail())
//                    .subject(renderedSubject)
//                    .body(renderedBody)
//                    .fromAddress(templateEntity.getFromAddress())
//                    .build();
//
//            kafkaTemplate.send("email-notifications", payload);
//
//            log.info("Password reset OTP generated for user {}. Request ID: {}", user.getId(), requestId);
//            return requestId;
//        } catch (Exception e) {
//            log.error("Exception occurred while request reset password for user {} with probable cause- ", usernameOrEmail, e);
//            throw new RuntimeException(e.getMessage());
//        }
//
//    }
//
//    /**
//     * Confirms the password reset by validating the OTP and updating the user's password.
//     *
//     * @param requestId The unique ID of the password reset request.
//     * @param otp The 6-digit one-time password.
//     * @param product The product scope for the user. Can be null for SUPER_ADMINs.
//     * @throws UserManagementException if the request is invalid, expired, or the OTP is incorrect.
//     */
//    @Transactional
//    public void confirmPasswordReset(UUID requestId, String otp, Product product, String ipAddress) {
//        log.debug("Confirming password reset for request ID: {}", requestId);
//        PasswordResetOtpJpaEntity otpEntity = passwordResetOtpRepository.findByRequestId(requestId)
//                .orElseThrow(() -> new UserManagementException("Invalid password reset request ID.", null));
//
//        if (otpEntity.isUsed()) {
//            throw new UserManagementException("This password reset request has already been used.", null);
//        }
//
//        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
//            throw new UserManagementException("Password reset request has expired.", null);
//        }
//
//        if (!passwordEncoder.matches(otp, otpEntity.getOtpHash())) {
//            throw new UserManagementException("Invalid OTP.", null);
//        }
//
//        // Generate a secure random 8-character password
//        String newPassword = generateSecurePassword();
//
//        // All checks passed, update the password in Keycloak
//        UserJpaEntity user = otpEntity.getUser();
//        keycloakService.updateUserPassword(user.getKeycloakId(), newPassword);
//
//        // Mark OTP as used
//        otpEntity.setUsed(true);
//        passwordResetOtpRepository.save(otpEntity);
//
//        // Try to send confirmation email, but don't fail the reset if template is missing
//        try {
//            String templateName = "PASSWORD_RESET_SUCCESS";
//            // Assuming we take the first product if available, or null
//            Product productContext = (user.getProducts() != null && !user.getProducts().isEmpty()) ? user.getProducts().getFirst() : null;
//
//            emailTemplateService.findActiveTemplate(templateName, productContext).ifPresent(templateEntity -> {
//                Map<String, String> variables = Map.of(
//                        "username",    user.getUsername(),
//                        "role", user.getRoles().stream().findFirst().orElse("USER"),
//                        "newPassword", newPassword,
//                        "email",       user.getEmail(),
//                        "resetAt",     LocalDateTime.now().toString(),
//                        "ipAddress", ipAddress
//                );
//                String renderedSubject = emailTemplateService.renderTemplate(templateEntity.getSubject(), variables);
//                String renderedBody = emailTemplateService.renderTemplate(templateEntity.getBody(), variables);
//
//                EmailEventPayload payload = EmailEventPayload.builder()
//                        .recipientEmail(user.getEmail())
//                        .subject(renderedSubject)
//                        .body(renderedBody)
//                        .fromAddress(templateEntity.getFromAddress())
//                        .build();
//                kafkaTemplate.send("email-notifications", payload);
//            });
//        } catch (Exception e) {
//            log.error("Failed to send password reset confirmation email for user {}", user.getId(), e);
//        }
//
//        log.info("Password successfully reset for user {}", user.getId());
//    }
//
//    /**
//     * Retrieves a user entity by its unique identifier (UUID).
//     * This is a read-only operation that fetches data exclusively from the local database.
//     *
//     * @param userId The UUID of the user to retrieve. Must not be null.
//     * @return The User entity corresponding to the provided ID.
//     * @throws UserNotFoundException If no user exists with the provided ID.
//     * @throws UserManagementException If an unexpected database error occurs.
//     */
//    @Transactional(readOnly = true)
//    @Override
//    public User getUserById(UUID userId) {
//        log.debug("Attempting to retrieve user by ID: {}", userId);
//        try {
//            return userRepository.findById(userId)
//                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
//        } catch (UserNotFoundException e) {
//            log.warn("User lookup failed: {}", e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error retrieving user with ID: {}", userId, e);
//            throw new UserManagementException("Failed to retrieve user by ID", e);
//        }
//    }
//
//    /**
//     * Retrieves a user entity by their exact username.
//     * This is a read-only operation querying the local database.
//     *
//     * @param username The exact string representation of the username. Must not be null or empty.
//     * @return The User entity associated with the username.
//     * @throws UserNotFoundException If no user exists with the provided username.
//     * @throws UserManagementException If an unexpected error occurs during the lookup.
//     */
//    @Transactional(readOnly = true)
//    @Override
//    public User getUserByUsername(String username) {
//        log.debug("Attempting to retrieve user by username: {}", username);
//        try {
//            return userRepository.findByUsername(username)
//                    .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
//        } catch (UserNotFoundException e) {
//            log.warn("User lookup failed: {}", e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error retrieving user with username: {}", username, e);
//            throw new UserManagementException("Failed to retrieve user by username", e);
//        }
//    }
//
//    /**
//     * Retrieves a complete list of all users registered in the local database.
//     * Warning: This operation can be memory-intensive on large datasets. Use pagination for production queries.
//     *
//     * @return A list containing all User entities. Returns an empty list if no users exist.
//     * @throws UserManagementException If a database error occurs during retrieval.
//     */
//    @Transactional(readOnly = true)
//    @Override
//    public List<User> getAllUsers() {
//        log.debug("Initiating retrieval of all users from the database.");
//        try {
//            return userRepository.findAll();
//        } catch (Exception e) {
//            log.error("Failed to retrieve the list of all users.", e);
//            throw new UserManagementException("An error occurred while fetching all users", e);
//        }
//    }
//
//    /**
//     * Retrieves a paginated subset of users from the local database.
//     * Recommended approach for retrieving user lists to ensure consistent performance and low memory overhead.
//     *
//     * @param pageable Contains pagination information such as page number, page size, and sorting criteria.
//     * @return A Page object containing the subset of User entities and pagination metadata.
//     * @throws UserManagementException If a database error occurs during the paginated query.
//     */
//    @Transactional(readOnly = true)
//    @Override
//    public Page<User> getUsers(Pageable pageable) {
//        log.debug("Retrieving users with pagination parameters: {}", pageable);
//        try {
//            return userRepository.findAll(pageable);
//        } catch (Exception e) {
//            log.error("Failed to retrieve paginated users with parameters: {}", pageable, e);
//            throw new UserManagementException("An error occurred while fetching paginated users", e);
//        }
//    }
//
//    /**
//     * Updates basic profile information (First Name, Last Name, Email, Status) for an existing user.
//     * Currently, this only updates the local database.
//     * Note: Future iterations must synchronize these profile changes back to Keycloak.
//     *
//     * @param userId The UUID of the user being updated.
//     * @param updatedUser A User object containing the new profile data.
//     * @return The updated User entity as saved in the database.
//     * @throws UserNotFoundException If the specified user ID does not exist.
//     * @throws UserManagementException If an error occurs while saving the updated data.
//     */
//    @Transactional
//    @Override
//    public User updateUser(UUID userId, User updatedUser) {
//        log.debug("Initiating profile update for user ID: {}", userId);
//        try {
//            User existingUser = getUserById(userId);
//
//            // Update local database fields
//            existingUser.setFirstName(updatedUser.getFirstName());
//            existingUser.setLastName(updatedUser.getLastName());
//            existingUser.setEmail(updatedUser.getEmail());
//            existingUser.setEnabled(updatedUser.getEnabled());
//            existingUser.setUpdatedAt(LocalDateTime.now());
//
//            User savedUser = userRepository.save(existingUser);
//            log.info("Successfully updated profile for user ID: {}", userId);
//
//            // TODO: Ensure data consistency by pushing profile changes to Keycloak via KeycloakService.
//            return savedUser;
//
//        } catch (UserNotFoundException e) {
//            throw e; // Let the specific not found exception bubble up
//        } catch (Exception e) {
//            log.error("Failed to update profile for user ID: {}. Error: {}", userId, e.getMessage(), e);
//            throw new UserManagementException("Failed to update user profile.", e);
//        }
//    }
//
//    /**
//     * Appends a new role and/or new products to a user's existing access profile.
//     * This operation is strictly additive; existing roles or products are not removed.
//     * Synchronizes role assignments with Keycloak to maintain access integrity.
//     *
//     * @param userId The UUID of the target user.
//     * @param newRole The string identifier of the role to add. Ignored if null or already present.
//     * @param products A list of Product enums to grant access to. Ignored if null. Duplicates are skipped.
//     * @return The updated User entity reflecting the new access grants.
//     * @throws UserNotFoundException If the user ID cannot be found.
//     * @throws KeycloakInteractionException If communication with the Keycloak server fails during role assignment.
//     * @throws UserManagementException If an error occurs while persisting the changes to the local database.
//     */
//    @Transactional
//    @Override
//    public User updateUserRoleAndProducts(UUID userId, String newRole, List<Product> products) {
//        log.debug("Attempting to append role [{}] and products {} to user ID: {}", newRole, products, userId);
//
//        User user = getUserById(userId);
//
//        try {
//            boolean isModified = false;
//
//            // 1. Conditionally append new role and sync with Keycloak
//            if (newRole != null && !user.getRoles().contains(newRole)) {
//                log.debug("Assigning new role '{}' to user {} in Keycloak.", newRole, userId);
//                keycloakService.assignRoleToUser(user.getKeycloakId(), newRole);
//                user.getRoles().add(newRole);
//                isModified = true;
//            }
//
//            // 2. Conditionally append new products
//            if (products != null) {
//                for (Product product : products) {
//                    if (!user.getProducts().contains(product)) {
//                        user.getProducts().add(product);
//                        isModified = true;
//                    }
//                }
//            }
//
//            // 3. Persist changes if any modifications occurred
//            if (isModified) {
//                user.setUpdatedAt(LocalDateTime.now());
//                User savedUser = userRepository.save(user);
//                log.info("Successfully updated access profile for user ID: {}", userId);
//                return savedUser;
//            } else {
//                log.debug("No new roles or products required appending for user ID: {}", userId);
//                return user;
//            }
//
//        } catch (RuntimeException e) {
//             // We catch RuntimeException specifically here because the Keycloak client often throws un-checked exceptions.
//             // We want to wrap these to distinguish IdP failures from our internal DB failures.
//            log.error("Failed to assign role to user {} in Keycloak. Reason: {}", userId, e.getMessage(), e);
//            throw new KeycloakInteractionException("Failed to synchronize role assignment with Identity Provider.", e);
//        } catch (Exception e) {
//            log.error("Unexpected error updating role/products for user {}: {}", userId, e.getMessage(), e);
//            throw new UserManagementException("Failed to update user access profile.", e);
//        }
//    }
//
//    /**
//     * Executes a user-initiated password change request.
//     * This method delegates the credential update directly to Keycloak.
//     *
//     * @param command A DTO containing the user ID and the new password string.
//     * @throws UserNotFoundException If the user ID in the command is invalid.
//     * @throws KeycloakInteractionException If the Keycloak API rejects the password update (e.g., policy violation or network error).
//     */
//    @Transactional
//    @Override
//    public void changePassword(ChangePasswordCommand command, String ipAddress) {
//        log.debug("Processing password change request for user: {}", command.username());
//        try {
//            User user = userRepository.findByUsername(command.username())
//                    .orElseThrow(() -> new UserNotFoundException("User not found for the specified product."));
//
//            // 1. Verify the current password against Keycloak
//            boolean isCurrentPasswordValid = keycloakService.verifyUserCredentials(user.getUsername(), command.currentPassword());
//            if (!isCurrentPasswordValid) {
//                throw new UserManagementException("The current password provided is incorrect.", null);
//            }
//
//            // 2. Update the password in Keycloak
//            keycloakService.updateUserPassword(user.getKeycloakId(), command.newPassword());
//
//            // 3. Send email notification
//            try {
//                String templateName = "PASSWORD_CHANGE_SUCCESS";
//                Product productContext = (user.getProducts() != null && !user.getProducts().isEmpty()) ? user.getProducts().stream().findFirst().orElse(null) : null;
//
//                emailTemplateService.findActiveTemplate(templateName, productContext).ifPresent(templateEntity -> {
//                    Map<String, String> variables = Map.of(
//                            "username", user.getUsername(),
//                            "changedAt", LocalDateTime.now().toString(),
//                            "email", user.getEmail(),
//                            "ipAddress", ipAddress,
//                            "role", user.getRoles().stream().findFirst().orElse("USER")
//                    );
//                    String renderedSubject = emailTemplateService.renderTemplate(templateEntity.getSubject(), variables);
//                    String renderedBody = emailTemplateService.renderTemplate(templateEntity.getBody(), variables);
//
//                    EmailEventPayload payload = EmailEventPayload.builder()
//                            .recipientEmail(user.getEmail())
//                            .subject(renderedSubject)
//                            .body(renderedBody)
//                            .fromAddress(templateEntity.getFromAddress())
//                            .build();
//                    kafkaTemplate.send("email-notifications", payload);
//                });
//            } catch (Exception e) {
//                log.error("Failed to send password change confirmation email for user {}", user.getId(), e);
//            }
//
//            log.info("Password successfully changed via Keycloak for user ID: {}", command.userId());
//        } catch (UserNotFoundException | UserManagementException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("Failed to execute password change for user ID: {}. Error: {}", command.userId(), e.getMessage(), e);
//            throw new KeycloakInteractionException("Failed to update credentials in Identity Provider.", e);
//        }
//    }
//
//    /**
//     * Permanently deletes a user from the system.
//     * This involves a distributed transaction: first removing the identity from Keycloak,
//     * then purging the local database record.
//     *
//     * @param userId The UUID of the user to be purged.
//     * @throws UserNotFoundException If the user ID does not exist locally.
//     * @throws KeycloakInteractionException If Keycloak fails to delete the user, leaving the system in a potentially inconsistent state.
//     * @throws UserManagementException If the local database deletion fails after Keycloak deletion.
//     */
//    @Transactional
//    @Override
//    public void deleteUser(UUID userId) {
//        log.debug("Initiating permanent deletion sequence for user ID: {}", userId);
//
//        User user = getUserById(userId);
//
//        // Step 1: Remove from Identity Provider
//        try {
//            log.debug("Requesting user deletion from Keycloak for external ID: {}", user.getKeycloakId());
//            keycloakService.deleteUser(user.getKeycloakId());
//        } catch (Exception e) {
//             log.error("Failed to delete user {} from Keycloak. Halting deletion process. Error: {}", userId, e.getMessage(), e);
//             throw new KeycloakInteractionException("Failed to remove user identity from external provider. Aborting deletion.", e);
//        }
//
//        // Step 2: Remove from local persistence
//        try {
//            log.debug("Removing user record from local database for ID: {}", userId);
//            userRepository.delete(user);
//            log.info("User deletion sequence completed successfully for user ID: {}", userId);
//        } catch (Exception e) {
//            log.error("CRITICAL: User {} was deleted from Keycloak, but local database deletion failed. System is in an inconsistent state.", userId, e);
//            throw new UserManagementException("Local record deletion failed after removing identity provider record.", e);
//        }
//    }
//
//    /**
//     * Generates a cryptographically secure random 8-character password that satisfies
//     * standard password complexity requirements.
//     *
//     * <p>The generated password is guaranteed to contain at least:
//     * <ul>
//     *   <li>One uppercase letter (A–Z)</li>
//     *   <li>One lowercase letter (a–z)</li>
//     *   <li>One numeric digit (0–9)</li>
//     *   <li>One special character from the set {@code !@#$%&*}</li>
//     * </ul>
//     *
//     * <p>The remaining 4 characters are randomly drawn from the combined pool of all
//     * the above character sets. The final character sequence is shuffled using
//     * {@link Collections#shuffle(java.util.List, java.util.Random)} with a
//     * {@link SecureRandom} instance to ensure the position of guaranteed characters
//     * is unpredictable.
//     *
//     * <p><strong>Security note:</strong> This method uses {@link SecureRandom} which is
//     * cryptographically strong and suitable for security-sensitive credential generation.
//     * It should never be replaced with {@code Random} or {@code ThreadLocalRandom}.
//     *
//     * @return a randomly generated 8-character password string meeting complexity requirements
//     */
//    private String generateSecurePassword() {
//        String uppercase  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
//        String lowercase  = "abcdefghijklmnopqrstuvwxyz";
//        String digits     = "0123456789";
//        String special    = "!@#$%&*";
//        String allChars   = uppercase + lowercase + digits + special;
//        SecureRandom random = new SecureRandom();
//        // Guarantee at least one character from each category
//        List<Character> passwordChars = new ArrayList<>();
//        passwordChars.add(uppercase.charAt(random.nextInt(uppercase.length())));
//        passwordChars.add(lowercase.charAt(random.nextInt(lowercase.length())));
//        passwordChars.add(digits.charAt(random.nextInt(digits.length())));
//        passwordChars.add(special.charAt(random.nextInt(special.length())));
//        // Fill remaining 4 characters from the full pool
//        for (int i = 4; i < 8; i++) {
//            passwordChars.add(allChars.charAt(random.nextInt(allChars.length())));
//        }
//        // Shuffle to avoid predictable position of guaranteed characters
//        Collections.shuffle(passwordChars, random);
//        StringBuilder password = new StringBuilder();
//        passwordChars.forEach(password::append);
//        return password.toString();
//    }
//}
