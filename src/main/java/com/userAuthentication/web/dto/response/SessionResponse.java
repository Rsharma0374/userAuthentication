package com.userAuthentication.web.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Session response DTO
 */
@Data
@Builder
public class SessionResponse {
    private String sessionId;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    private boolean isCurrentSession;
}