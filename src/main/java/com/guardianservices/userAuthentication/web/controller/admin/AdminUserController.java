//package com.guardianservices.userAuthentication.web.controller.admin;
//
//import com.guardianservices.userAuthentication.domain.model.User;
//import com.guardianservices.userAuthentication.web.dto.request.UpdateUserRequest;
//import com.guardianservices.userAuthentication.web.dto.response.PagedResponse;
//import com.guardianservices.userAuthentication.web.dto.response.UserResponse;
//import com.guardianservices.userAuthentication.web.mapper.UserMapper;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/admin/users")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "Admin User Management", description = "Endpoints for managing users")
//@SecurityRequirement(name = "bearer-jwt")
//public class AdminUserController {
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
//}
