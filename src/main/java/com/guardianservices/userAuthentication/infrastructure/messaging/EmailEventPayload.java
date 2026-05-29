package com.guardianservices.userAuthentication.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailEventPayload {
    private UUID requestId;
    private String recipientEmail;
    private String subject;
    private String body;
    private String fromAddress;
}
