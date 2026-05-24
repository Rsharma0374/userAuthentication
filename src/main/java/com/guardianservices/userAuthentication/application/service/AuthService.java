package com.guardianservices.userAuthentication.application.service;

import com.guardianservices.userAuthentication.application.command.LoginCommand;
import com.guardianservices.userAuthentication.application.command.RegisterCommand;
import com.guardianservices.userAuthentication.application.command.*;
import com.guardianservices.userAuthentication.domain.event.UserLoggedInEvent;
import com.guardianservices.userAuthentication.domain.event.UserRegisteredEvent;
import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.domain.model.User;
import com.guardianservices.userAuthentication.domain.port.in.AuthUseCase;
import com.guardianservices.userAuthentication.domain.port.out.AuditEventPublisher;
import com.guardianservices.userAuthentication.domain.port.out.UserRepository;
import com.guardianservices.userAuthentication.web.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.guardianservices.userAuthentication.conf.KeycloakConfig.*;

/**
 * Authentication service with Java 21 features including pattern matching
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements AuthUseCase {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final AuditEventPublisher eventPublisher;

    @Autowired
    private InfisicalService infisicalService;

    /**
     * Authenticates user using pattern matching for error handling
     */
    @Transactional
    @Override
    public TokenResponse login(LoginCommand command) {
        log.debug("Authenticating user: {}", command.username());

        return switch (validateUser(command.username())) {
            case UserValid(var user) -> processLogin(command, user);
            case UserNotFound() -> throw new RuntimeException("User not found");
            case UserDisabled() -> throw new RuntimeException("User account is disabled");
            case UserSuspended(var reason) -> throw new RuntimeException(String.format("User account suspended: %s", reason));
        };
    }

    /**
     * Pattern matching sealed interface for user validation results
     */
    private sealed interface UserValidationResult permits UserValid, UserNotFound, UserDisabled, UserSuspended {}

    private record UserValid(User user) implements UserValidationResult {}
    private record UserNotFound() implements UserValidationResult {}
    private record UserDisabled() implements UserValidationResult {}
    private record UserSuspended(String reason) implements UserValidationResult {}

    private UserValidationResult validateUser(String username) {
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(username, username);

        if (userOptional.isEmpty()) {
            return new UserNotFound();
        }

        User user = userOptional.get();

        if (!user.getEnabled()) {
            return new UserDisabled();
        }

        if (user.getLastLogin() != null && user.getLastLogin().isBefore(LocalDateTime.now().minusMonths(6))) {
            return new UserSuspended("Inactive for more than 6 months");
        }

        return new UserValid(user);
    }

    private TokenResponse processLogin(LoginCommand command, User user) {
        var tokenResponse = getTokensFromKeycloak(command.username(), command.password());

        tokenResponse.setUserId(user.getId());
        tokenResponse.setEmailVerified(user.getEmailVerified());
        tokenResponse.setMfaEnabled(user.getMfaEnabled());
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Publish event using Java 21 record
        eventPublisher.publishUserLoggedIn(
                new UserLoggedInEvent(user.getId(), user.getUsername(), command.ipAddress(), command.userAgent())
        );

        log.info("User logged in successfully: {}", command.username());
        return tokenResponse;
    }

    /**
     * Registers new user with pattern matching
     */
    @Transactional
    @Override
    public User register(RegisterCommand command) {
        log.debug("Registering new user: {}", command.username());

        return switch (checkUserExists(command.username(), command.email())) {
            case UserExists() -> throw new RuntimeException("User already exists");
            case EmailExists() -> throw new RuntimeException("Email already registered");
            case UserNotExists() -> createUser(command, "USER", command.products());
        };
    }

    /**
     * Registers new admin user
     */
    @Transactional
    @Override
    public User registerAdmin(RegisterCommand command) {
        log.debug("Registering new admin user: {}", command.username());

        String role = command.products() == null || command.products().isEmpty() ? "SUPER_ADMIN" : "ADMIN";

        return switch (checkUserExists(command.username(), command.email())) {
            case UserExists() -> throw new RuntimeException("Admin user already exists");
            case EmailExists() -> throw new RuntimeException("Email already registered for an admin user");
            case UserNotExists() -> createUser(command, role, command.products());
        };
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        var tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", infisicalService.getSecret(KEYKLOAK_URL, KEYKLOAK_SECRET_TYPE), infisicalService.getSecret(KEYKLOAK_REALM, KEYKLOAK_SECRET_TYPE));

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", infisicalService.getSecret(KEYKLOAK_CLIENT_ID, KEYKLOAK_SECRET_TYPE));
        body.add("client_secret", infisicalService.getSecret(KEYKLOAK_CLIENT_SECRET, KEYKLOAK_SECRET_TYPE));
        body.add("refresh_token", refreshToken);

        var request = new HttpEntity<>(body, headers);
        var response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, AccessTokenResponse.class);
        var tokenResponse = response.getBody();

        return TokenResponse.builder()
                .accessToken(Objects.requireNonNull(tokenResponse).getToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .refreshExpiresIn(tokenResponse.getRefreshExpiresIn())
                .tokenType(tokenResponse.getTokenType())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        var tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/logout", infisicalService.getSecret(KEYKLOAK_URL, KEYKLOAK_SECRET_TYPE), infisicalService.getSecret(KEYKLOAK_REALM, KEYKLOAK_SECRET_TYPE));

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", infisicalService.getSecret(KEYKLOAK_CLIENT_ID, KEYKLOAK_SECRET_TYPE));
        body.add("client_secret", infisicalService.getSecret(KEYKLOAK_CLIENT_SECRET, KEYKLOAK_SECRET_TYPE));
        body.add("refresh_token", refreshToken);

        var request = new HttpEntity<>(body, headers);
        restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Void.class);
    }

    private sealed interface UserExistsResult permits UserExists, EmailExists, UserNotExists {}
    private record UserExists() implements UserExistsResult {}
    private record EmailExists() implements UserExistsResult {}
    private record UserNotExists() implements UserExistsResult {}

    private UserExistsResult checkUserExists(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            return new UserExists();
        }
        if (userRepository.existsByEmail(email)) {
            return new EmailExists();
        }
        return new UserNotExists();
    }

    private User createUser(RegisterCommand command, String roleName, Set<Product> products) {
        var user = User.builder()
                .username(command.username())
                .email(command.email())
                .firstName(command.firstName())
                .lastName(command.lastName())
                .enabled(true)
                .emailVerified(false)
                .roles(new HashSet<>(Set.of(roleName)))
                .products(products != null ? new HashSet<>(products) : new HashSet<>())
                .build();

        var keycloakUser = keycloakService.createUserInKeycloak(user, command.password());
        user.setKeycloakId(keycloakUser.getId());
        keycloakService.assignRoleToUser(keycloakUser.getId(), roleName);

        log.info("User is {}", user);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        // Publish user registered event
        eventPublisher.publishUserRegistered(
                new UserRegisteredEvent(user.getId(), user.getUsername(), user.getEmail(),
                        user.getFirstName(), user.getLastName())
        );

        log.info("User registered successfully: {}", command.username());
        return user;
    }

    private TokenResponse getTokensFromKeycloak(String username, String password) {
        var tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", infisicalService.getSecret(KEYKLOAK_URL, KEYKLOAK_SECRET_TYPE), infisicalService.getSecret(KEYKLOAK_REALM, KEYKLOAK_SECRET_TYPE));

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", infisicalService.getSecret(KEYKLOAK_CLIENT_ID, KEYKLOAK_SECRET_TYPE));
        body.add("client_secret", infisicalService.getSecret(KEYKLOAK_CLIENT_SECRET, KEYKLOAK_SECRET_TYPE));
        body.add("username", username);
        body.add("password", password);

        var request = new HttpEntity<>(body, headers);
        var response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, AccessTokenResponse.class);
        var tokenResponse = response.getBody();

        return TokenResponse.builder()
                .accessToken(Objects.requireNonNull(tokenResponse).getToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .refreshExpiresIn(tokenResponse.getRefreshExpiresIn())
                .tokenType(tokenResponse.getTokenType())
                .build();
    }
}
