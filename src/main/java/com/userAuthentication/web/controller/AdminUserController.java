package com.userAuthentication.web.controller;

import com.userAuthentication.application.service.UserService;
import com.userAuthentication.domain.model.User;
import com.userAuthentication.web.dto.response.UserResponse;
import com.userAuthentication.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Retrieves all users (admin only)
     *
     * @return list of all users
     */
    @GetMapping
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
    @Operation(summary = "Delete user", description = "Permanently deletes a user from the system")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        log.debug("Admin deleting user: {}", userId);

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
