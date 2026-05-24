package com.guardianservices.userAuthentication.web.controller;

import com.guardianservices.userAuthentication.application.command.RegisterCommand;
import com.guardianservices.userAuthentication.application.service.AuthService;
import com.guardianservices.userAuthentication.application.service.UserService;
import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.domain.model.User;
import com.guardianservices.userAuthentication.web.dto.request.AdminRegisterRequest;
import com.guardianservices.userAuthentication.web.dto.request.UpdateUserRequest;
import com.guardianservices.userAuthentication.web.dto.response.ApiErrorResponse;
import com.guardianservices.userAuthentication.web.dto.response.UserResponse;
import com.guardianservices.userAuthentication.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for user management operations
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin User Management", description = "Admin endpoints for user management")
@SecurityRequirement(name = "bearer-jwt")
public class AdminUserController {

    private final UserService userService;
    private final AuthService authService;
    private final UserMapper userMapper;

    /**
     * Creates a super admin or a product admin user
     *
     * @param request registration request with admin user details
     * @return created admin user response
     */
    @PostMapping("/create")
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create admin user", description = "Creates a new super admin or product admin user")
    public ResponseEntity<?> createAdminUser(@Valid @RequestBody AdminRegisterRequest request) {
        log.debug("Admin creating new admin user: {}", request.getUsername());

        try {
            RegisterCommand command = new RegisterCommand(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getProducts()
            );

            var user = authService.registerAdmin(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
        } catch (Exception e) {
            log.error("Error during admin creation for user: {}", request.getUsername(), e);

            HttpStatus status = HttpStatus.BAD_REQUEST;
            String errorType = "Bad Request";

            if (e.getMessage() != null && (e.getMessage().contains("already exists") || e.getMessage().contains("already registered"))) {
                status = HttpStatus.CONFLICT;
                errorType = "Conflict";
            }

            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(status.value())
                    .error(errorType)
                    .message(e.getMessage())
                    .path("/admin/users/create")
                    .build();
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    /**
     * Updates a user's role and product access
     *
     * @param request update request with user ID, new role, and product
     * @return updated user response
     */
    @PutMapping("/update-user")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Update user role and product", description = "Updates a user's role and product access")
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        log.debug("Updating user: {}", request.getUserId());

        List<Product> productsToUpdate = request.getProductName() == null ?
                Collections.emptyList() :
                Collections.singletonList(request.getProductName());

        User updatedUser = userService.updateUserRoleAndProducts(
                request.getUserId(),
                request.getRole(),
                productsToUpdate
        );

        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }

    /**
     * Retrieves all users (admin only)
     *
     * @return list of all users
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves all users in the system")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.debug("Admin retrieving all users");

        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves user by ID (admin only)
     *
     * @param userId user ID
     * @return user response
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.debug("Admin retrieving user by ID: {}", userId);

        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    /**
     * Enables or disables a user account
     *
     * @param userId user ID
     * @param enabled enabled status
     * @return updated user response
     */
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Enables or disables a user account")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam boolean enabled) {

        log.debug("Admin updating user status: {} - enabled: {}", userId, enabled);

        User user = userService.getUserById(userId);
        user.setEnabled(enabled);
        user = userService.updateUser(userId, user);

        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    /**
     * Deletes a user (admin only)
     *
     * @param userId user ID
     * @return no content response
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Permanently deletes a user from the system")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        log.debug("Admin deleting user: {}", userId);

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
