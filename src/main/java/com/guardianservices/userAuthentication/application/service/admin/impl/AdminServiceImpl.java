package com.guardianservices.userAuthentication.application.service.admin.impl;

import com.guardianservices.userAuthentication.application.command.LoginCommand;
import com.guardianservices.userAuthentication.application.command.RegisterCommand;
import com.guardianservices.userAuthentication.application.exception.ProductNotFoundException;
import com.guardianservices.userAuthentication.application.exception.UserNotFoundException;
import com.guardianservices.userAuthentication.application.service.AdminKeycloakService;
import com.guardianservices.userAuthentication.application.service.InfisicalService;
import com.guardianservices.userAuthentication.application.service.ProductService;
import com.guardianservices.userAuthentication.application.service.admin.AdminService;
import com.guardianservices.userAuthentication.domain.event.UserLoggedInEvent;
import com.guardianservices.userAuthentication.domain.event.UserRegisteredEvent;
import com.guardianservices.userAuthentication.domain.model.User;
import com.guardianservices.userAuthentication.domain.model.results.UserExistsResult;
import com.guardianservices.userAuthentication.domain.port.out.AdminRepository;
import com.guardianservices.userAuthentication.domain.port.out.AuditEventPublisher;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository.AdminJpaRepository;
import com.guardianservices.userAuthentication.web.dto.response.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.guardianservices.userAuthentication.conf.KeycloakConfig.*;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Autowired
    private ProductService productService;

    @Autowired
    private AdminKeycloakService adminKeycloakService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AuditEventPublisher eventPublisher;

    @Autowired
    private InfisicalService infisicalService;

    private final RestTemplate restTemplate;

    public AdminServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Transactional
    @Override
    public User registerAdmin(RegisterCommand command) {
        log.debug("Registering new admin user: {}", command.username());

        if (StringUtils.isNotBlank(command.product())) {
            if (!productService.checkProductExistence(command.product())) {
                throw new ProductNotFoundException("Product not found.");
            }
        }

        String role = StringUtils.isBlank(command.product()) ? "SUPER_ADMIN" : "PRODUCT_ADMIN";

        return switch (checkUserExists(command.username(), command.product() != null ? command.product() : null)) {
            case UserExistsResult.UserExists() -> throw new RuntimeException("Admin user already exists");
            case UserExistsResult.EmailExists() ->
                    throw new RuntimeException("Email already registered for an admin user");
            case UserExistsResult.UserNotExists() -> createUser(command, role, command.product());
        };
    }

    private UserExistsResult checkUserExists(String username, String product) {
        if (adminRepository.existsByUsernameAndProduct(username, product)) {
            return new UserExistsResult.UserExists();
        }
        return new UserExistsResult.UserNotExists();
    }

    private User createUser(RegisterCommand command, String roleName, String product) {
        var user = User.builder()
                .username(command.username())
                .email(command.email())
                .firstName(command.firstName())
                .lastName(command.lastName())
                .enabled(true)
                .emailVerified(false)
                .role(roleName)
                .product(product)
                .build();

        var keycloakUser = adminKeycloakService.createUserInKeycloak(user, command.password());
        user.setKeycloakId(keycloakUser.getId());
        adminKeycloakService.assignRoleToUser(keycloakUser.getId(), roleName);

        log.info("User is {}", user);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = adminRepository.save(user);

        eventPublisher.publishUserRegistered(
                new UserRegisteredEvent(user.getId(), user.getUsername(), user.getEmail(),
                        user.getFirstName(), user.getLastName())
        );

        log.info("User registered successfully: {}", command.username());
        return user;
    }

    private sealed interface UserValidationResult permits UserValid, UserNotFound, UserDisabled, UserSuspended {
    }

    private record UserValid(User user) implements UserValidationResult {
    }

    private record UserNotFound() implements UserValidationResult {
    }

    private record UserDisabled() implements UserValidationResult {
    }

    private record UserSuspended(String reason) implements UserValidationResult {
    }

    @Transactional
    @Override
    public TokenResponse login(LoginCommand command) {
        log.debug("Authenticating user: {} for product: {}", command.username(), command.product());

        return switch (validateUser(command.username(), command.product())) {
            case UserValid(var user) -> processLogin(command, user);
            case UserNotFound() -> throw new UserNotFoundException("User not found for the specified product");
            case UserDisabled() -> throw new RuntimeException("User account is disabled");
            case UserSuspended(var reason) ->
                    throw new RuntimeException(String.format("User account suspended: %s", reason));
        };
    }

    private UserValidationResult validateUser(String username, String product) {
        Optional<User> userOptional = adminRepository.findByUsernameAndProduct(username, product);

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
        adminRepository.save(user);

        eventPublisher.publishUserLoggedIn(
                new UserLoggedInEvent(user.getId(), user.getUsername(), command.ipAddress(), command.userAgent())
        );

        log.info("User logged in successfully: {}", command.username());
        return tokenResponse;
    }

        private TokenResponse getTokensFromKeycloak(String username, String password) {
            var tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", infisicalService.getSecret(KEYCLOAK_URL, KEYCLOAK_SECRET_TYPE), infisicalService.getSecret(KEYCLOAK_REALM, KEYCLOAK_SECRET_TYPE));

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", infisicalService.getSecret(KEYCLOAK_ADMIN_CLIENT_ID, KEYCLOAK_SECRET_TYPE));
            body.add("client_secret", infisicalService.getSecret(KEYCLOAK_ADMIN_CLIENT_SECRET, KEYCLOAK_SECRET_TYPE));
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

        @Override
    public TokenResponse refreshToken(String refreshToken) {
        var tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", infisicalService.getSecret(KEYCLOAK_URL, KEYCLOAK_SECRET_TYPE), infisicalService.getSecret(KEYCLOAK_REALM, KEYCLOAK_SECRET_TYPE));

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", infisicalService.getSecret(KEYCLOAK_ADMIN_CLIENT_ID, KEYCLOAK_SECRET_TYPE));
        body.add("client_secret", infisicalService.getSecret(KEYCLOAK_ADMIN_CLIENT_SECRET, KEYCLOAK_SECRET_TYPE));
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
}
