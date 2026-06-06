package com.guardianservices.userAuthentication.web.controller.admin;

import com.guardianservices.userAuthentication.application.service.admin.AdminService;
import com.guardianservices.userAuthentication.domain.model.User;
import com.guardianservices.userAuthentication.util.Helper;
import com.guardianservices.userAuthentication.web.dto.request.UpdateUserRequest;
import com.guardianservices.userAuthentication.web.dto.response.PagedResponse;
import com.guardianservices.userAuthentication.web.dto.response.UserResponse;
import com.guardianservices.userAuthentication.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin User Management", description = "Endpoints for managing users")
@SecurityRequirement(name = "bearer-jwt")
public class AdminUserController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserMapper userMapper;

        /**
     * Retrieves a paginated list of users
     *
     * @param page page number (0-based)
     * @param limit page size
     * @return paginated list of users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get paginated users", description = "Retrieves a paginated list of users in the system")
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Jwt jwt) {

        log.debug("Retrieving users: page={}, limit={}", page, limit);

        String username = jwt.getClaimAsString("preferred_username");

        boolean isSuperAdmin = Helper.extractRoles(jwt).stream()
                .anyMatch(role ->
                        role.equals("SUPER_ADMIN"));

        // Adjusting because conventional REST APIs often use 1-based page, but Spring Data uses 0-based
        // Assuming the requested API implies 1-based pagination. If 'page' is 1, subtract 1 to get 0.
        int springPage = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(springPage, limit);

        Page<User> userPage = adminService.getUsers(pageable, isSuperAdmin, username);
        Page<UserResponse> responsePage = userPage.map(userMapper::toResponse);

        return ResponseEntity.ok(new PagedResponse<>(responsePage));
    }

//
//    private final UserService userService;
//    private final UserMapper userMapper;
//
//    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//    @Operation(summary = "Get Paginated Users", description = "Retrieves a paginated list of all users.")
//    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int limit) {
//
//        int springPage = page > 0 ? page - 1 : 0;
//        Pageable pageable = PageRequest.of(springPage, limit);
//        Page<User> userPage = userService.getUsers(pageable);
//        Page<UserResponse> responsePage = userPage.map(userMapper::toResponse);
//        return ResponseEntity.ok(new PagedResponse<>(responsePage));
//    }
//
//    @GetMapping("/{userId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//    @Operation(summary = "Get User by ID", description = "Retrieves a specific user by their unique ID.")
//    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
//        User user = userService.getUserById(userId);
//        return ResponseEntity.ok(userMapper.toResponse(user));
//    }
//
//    @PutMapping("/update")
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @Operation(summary = "Update User Role and Products", description = "Updates a user's role and product access.")
//    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest request) {
//        User updatedUser = userService.updateUserRoleAndProducts(
//                request.getUserId(),
//                request.getRole(),
//                request.getProducts()
//        );
//        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
//    }
//
//    @PatchMapping("/{userId}/status")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//    @Operation(summary = "Update User Status", description = "Enables or disables a user's account.")
//    public ResponseEntity<UserResponse> updateUserStatus(
//            @PathVariable UUID userId,
//            @RequestParam boolean enabled) {
//
//        User user = userService.getUserById(userId);
//        user.setEnabled(enabled);
//        user = userService.updateUser(userId, user);
//        return ResponseEntity.ok(userMapper.toResponse(user));
//    }
//
//    @DeleteMapping("/{userId}")
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @Operation(summary = "Delete User", description = "Permanently deletes a user from the system.")
//    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
//        userService.deleteUser(userId);
//        return ResponseEntity.noContent().build();
//    }
}
