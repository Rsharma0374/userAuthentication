//package com.guardianservices.userAuthentication.web.controller.user;
//
//import com.guardianservices.userAuthentication.application.command.ChangePasswordCommand;
//import com.guardianservices.userAuthentication.application.service.UserService;
//import com.guardianservices.userAuthentication.web.dto.request.ChangePasswordRequest;
//import com.guardianservices.userAuthentication.web.dto.response.ApiErrorResponse;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/portal/users")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "User Portal Management", description = "Endpoints for user self-service")
//@SecurityRequirement(name = "bearer-jwt")
//public class UserUserController {
//
//    private final UserService userService;
//
//    @PostMapping("/change-password")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "Change Password", description = "Allows an authenticated user to change their password.")
//    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal Jwt jwt, HttpServletRequest httpRequest) {
//        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//            return ResponseEntity.badRequest().body(new ApiErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", "Passwords do not match.", "/portal/users/change-password", null));
//        }
//        try {
//            String username = jwt.getClaimAsString("preferred_username");
//            ChangePasswordCommand command = new ChangePasswordCommand(UUID.fromString(jwt.getSubject()), username, request.getCurrentPassword(), request.getNewPassword(), request.getProduct());
//            userService.changePassword(command, httpRequest.getRemoteAddr());
//            return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", e.getMessage(), "/portal/users/change-password", null));
//        }
//    }
//}
