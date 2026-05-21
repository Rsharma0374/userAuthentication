package com.userAuthentication.web.controller;

import com.userAuthentication.application.service.UserService;
import com.userAuthentication.domain.model.User;
import com.userAuthentication.web.dto.response.PagedResponse;
import com.userAuthentication.web.dto.response.UserResponse;
import com.userAuthentication.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for general user operations
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Endpoints for user management")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Retrieves a paginated list of users
     *
     * @param page page number (0-based)
     * @param limit page size
     * @return paginated list of users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Get paginated users", description = "Retrieves a paginated list of users in the system")
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("Retrieving users: page={}, limit={}", page, limit);

        // Adjusting because conventional REST APIs often use 1-based page, but Spring Data uses 0-based
        // Assuming the requested API implies 1-based pagination. If 'page' is 1, subtract 1 to get 0.
        int springPage = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(springPage, limit);

        Page<User> userPage = userService.getUsers(pageable);
        
        Page<UserResponse> responsePage = userPage.map(userMapper::toResponse);

        return ResponseEntity.ok(new PagedResponse<>(responsePage));
    }
}
