package com.guardianservices.userAuthentication.application.service.admin;

import com.guardianservices.userAuthentication.application.command.LoginCommand;
import com.guardianservices.userAuthentication.application.command.RegisterCommand;
import com.guardianservices.userAuthentication.domain.model.User;
import com.guardianservices.userAuthentication.web.dto.response.TokenResponse;
import jakarta.validation.constraints.NotBlank;

public interface AdminService {

    /**
     * Registers a new admin user
     *
     * @param command register command
     * @return created admin user
     */
    User registerAdmin(RegisterCommand command);

    public TokenResponse login(LoginCommand command);

    TokenResponse refreshToken(@NotBlank(message = "Refresh token is required") String refreshToken);
}
