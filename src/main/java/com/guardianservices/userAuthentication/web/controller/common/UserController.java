//package com.guardianservices.userAuthentication.web.controller.common;
//
//import com.guardianservices.userAuthentication.application.command.ChangePasswordCommand;
//import com.guardianservices.userAuthentication.application.service.UserService;
//import com.guardianservices.userAuthentication.domain.model.User;
//import com.guardianservices.userAuthentication.util.HttpRequestUtils;
//import com.guardianservices.userAuthentication.web.dto.request.ChangePasswordRequest;
//import com.guardianservices.userAuthentication.web.dto.response.ApiErrorResponse;
//import com.guardianservices.userAuthentication.web.dto.response.PagedResponse;
//import com.guardianservices.userAuthentication.web.dto.response.UserResponse;
//import com.guardianservices.userAuthentication.web.mapper.UserMapper;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
///**
// * REST controller for general user operations
// */
//@RestController
//@RequestMapping("/users")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "User Management", description = "Endpoints for user management")
//@SecurityRequirement(name = "bearer-jwt")
//public class UserController {
//
//    private final UserService userService;
//    private final UserMapper userMapper;
//    private final HttpRequestUtils httpRequestUtils;
//
//    /**
//     * Retrieves a paginated list of users
//     *
//     * @param page page number (0-based)
//     * @param limit page size
//     * @return paginated list of users
//     */
//    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//    @Operation(summary = "Get paginated users", description = "Retrieves a paginated list of users in the system")
//    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int limit) {
//
//        log.debug("Retrieving users: page={}, limit={}", page, limit);
//
//        // Adjusting because conventional REST APIs often use 1-based page, but Spring Data uses 0-based
//        // Assuming the requested API implies 1-based pagination. If 'page' is 1, subtract 1 to get 0.
//        int springPage = page > 0 ? page - 1 : 0;
//        Pageable pageable = PageRequest.of(springPage, limit);
//
//        Page<User> userPage = userService.getUsers(pageable);
//
//        Page<UserResponse> responsePage = userPage.map(userMapper::toResponse);
//
//        return ResponseEntity.ok(new PagedResponse<>(responsePage));
//    }
//
//    /**
//     * Allows an authenticated user to change their own password.
//     *
//     * @param request The request containing the current and new passwords.
//     * @param jwt The JWT of the authenticated user, injected by Spring Security.
//     * @return A successful response if the password was changed.
//     */
//    @PostMapping("/change-password")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "Change user password", description = "Allows an authenticated user to change their own password")
//    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal Jwt jwt, HttpServletRequest httpRequest) {
//        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//            return ResponseEntity.badRequest().body(new ApiErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", "New password and confirmation password do not match.", "/users/change-password", null));
//        }
//
//        try {
//            UUID userId = UUID.fromString(jwt.getSubject());
//            // Extract the username from the JWT. The claim name depends on Keycloak configuration,
//            // usually it's "preferred_username"
//            String username = jwt.getClaimAsString("preferred_username");
//
//            if (username == null) {
//                 log.warn("Username claim not found in JWT for subject: {}", jwt.getSubject());
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized", "Invalid token structure.", "/users/change-password", null));
//            }
//            String ipAddress = httpRequestUtils.getClientIpAddress(httpRequest);
//
//            ChangePasswordCommand command = new ChangePasswordCommand(userId, username, request.getCurrentPassword(), request.getNewPassword(), request.getProduct());
//            userService.changePassword(command, ipAddress);
//            return ResponseEntity.ok().body("Password changed successfully.");
//        } catch (Exception e) {
//            log.error("Error during password change for user {}", jwt.getSubject(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", e.getMessage(), "/users/change-password", null));
//        }
//    }
//}
