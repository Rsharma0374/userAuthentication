package com.guardianservices.userAuthentication.web.controller.admin;

import com.guardianservices.userAuthentication.application.command.LoginCommand;
import com.guardianservices.userAuthentication.application.command.RegisterCommand;
//import com.guardianservices.userAuthentication.application.service.AuthService;
import com.guardianservices.userAuthentication.application.service.admin.AdminService;
import com.guardianservices.userAuthentication.web.dto.request.AdminRegisterRequest;
import com.guardianservices.userAuthentication.web.dto.request.LoginRequest;
import com.guardianservices.userAuthentication.web.dto.response.ApiErrorResponse;
import com.guardianservices.userAuthentication.web.dto.response.TokenResponse;
import com.guardianservices.userAuthentication.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Authentication", description = "Endpoints for admin registration")
public class AdminAuthController {


    @Autowired
    private AdminService adminService;

    private final UserMapper userMapper;

    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Register Admin or User", description = "Creates a new product admin, or super admin.")
    public ResponseEntity<?> register(@Valid @RequestBody AdminRegisterRequest request) {
        try {
            RegisterCommand command = new RegisterCommand(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getProductName()
            );
            var user = adminService.registerAdmin(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Registration Failed", e.getMessage(), "/admin/auth/register", null));
        }
    }

        /**
     * Authenticates user and returns access token
     *
     * @param request login request containing credentials
     * @return token response with access and refresh tokens
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns tokens")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login request for user: {}", request.getUsername());

        try {
            LoginCommand command = new LoginCommand(
                    request.getUsername(),
                    request.getPassword(),
                    null, // ipAddress
                    null,  // userAgent
                    request.getProduct()
            );

            TokenResponse response = adminService.login(command);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during login for user: {}", request.getUsername(), e);
            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message(e.getMessage())
                    .path("/auth/login")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
