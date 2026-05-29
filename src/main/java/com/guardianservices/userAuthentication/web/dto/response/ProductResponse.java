package com.guardianservices.userAuthentication.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {

    private String name;
    private String description;
    private LocalDateTime createdAt;
}
