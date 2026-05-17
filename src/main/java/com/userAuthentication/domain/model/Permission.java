package com.userAuthentication.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Permission entity representing fine-grained access rights
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    private UUID id;
    private String name;
    private String description;
    private String resource;  // e.g., "user", "role", "document"
    private String action;    // e.g., "create", "read", "update", "delete"
    private LocalDateTime createdAt;
}
