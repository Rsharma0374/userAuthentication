package com.guardianservices.userAuthentication.web.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Role response DTO
 */
@Data
@Builder
public class RoleResponse {
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
