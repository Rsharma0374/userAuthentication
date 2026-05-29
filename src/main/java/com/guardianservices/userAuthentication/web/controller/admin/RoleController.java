package com.guardianservices.userAuthentication.web.controller.admin;

import com.guardianservices.userAuthentication.application.command.AssignRoleCommand;
import com.guardianservices.userAuthentication.application.command.CreateRoleCommand;
import com.guardianservices.userAuthentication.application.command.RemoveRoleCommand;
import com.guardianservices.userAuthentication.application.service.RoleService;
import com.guardianservices.userAuthentication.domain.model.Role;
import com.guardianservices.userAuthentication.web.dto.request.AssignRoleRequest;
import com.guardianservices.userAuthentication.web.dto.request.CreateRoleRequest;
import com.guardianservices.userAuthentication.web.dto.response.RoleResponse;
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

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for role management operations
 * Requires admin privileges for most operations
 */
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "Endpoints for managing roles")
@SecurityRequirement(name = "bearer-jwt")
public class RoleController {

    private final RoleService roleService;

    /**
     * Creates a new role
     *
     * @param request create role request
     * @return created role response
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create role", description = "Creates a new role in the system")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.debug("Creating role: {}", request.getName());

        CreateRoleCommand command = new CreateRoleCommand(
                request.getName(),
                request.getDescription()
        );

        roleService.createRole(command);

        RoleResponse response = RoleResponse.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
//
//    /**
//     * Assigns a role to a user
//     *
//     * @param userId user ID
//     * @param request assign role request
//     * @return no content response
//     */
//    @PostMapping("/users/{userId}")
//    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
//    @Operation(summary = "Assign role to user", description = "Assigns a role to a specific user")
//    public ResponseEntity<Void> assignRoleToUser(
//            @PathVariable UUID userId,
//            @Valid @RequestBody AssignRoleRequest request) {
//
//        log.debug("Assigning role '{}' to user: {}", request.getRoleName(), userId);
//
//        AssignRoleCommand command = new AssignRoleCommand(
//                userId,
//                request.getRoleName(),
//                null // assignedBy
//        );
//
//        roleService.assignRoleToUser(command);
//
//        return ResponseEntity.noContent().build();
//    }
//
//    /**
//     * Removes a role from a user
//     *
//     * @param userId user ID
//     * @param request remove role request
//     * @return no content response
//     */
//    @DeleteMapping("/users/{userId}")
//    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
//    @Operation(summary = "Remove role from user", description = "Removes a role from a specific user")
//    public ResponseEntity<Void> removeRoleFromUser(
//            @PathVariable UUID userId,
//            @Valid @RequestBody AssignRoleRequest request) {
//
//        log.debug("Removing role '{}' from user: {}", request.getRoleName(), userId);
//
//        RemoveRoleCommand command = new RemoveRoleCommand(
//                userId,
//                request.getRoleName(),
//                null // removedBy
//        );
//
//        roleService.removeRoleFromUser(command);
//
//        return ResponseEntity.noContent().build();
//    }
//
    /**
     * Retrieves all roles
     *
     * @return list of roles
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Get all roles", description = "Retrieves all roles in the system")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        log.debug("Retrieving all roles");

        List<Role> roles = roleService.getAllRoles();

        List<RoleResponse> responses = roles.stream()
                .map(role -> RoleResponse.builder()
                        .name(role.getName())
                        .description(role.getDescription())
                        .createdAt(role.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
//
//    /**
//     * Retrieves roles for a specific user
//     *
//     * @param userId user ID
//     * @return set of user roles
//     */
//    @GetMapping("/users/{userId}")
//    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
//    @Operation(summary = "Get user roles", description = "Retrieves all roles assigned to a user")
//    public ResponseEntity<Set<String>> getUserRoles(@PathVariable UUID userId) {
//        log.debug("Retrieving roles for user: {}", userId);
//
//        Set<String> roles = roleService.getUserRoles(userId);
//        return ResponseEntity.ok(roles);
//    }
}
