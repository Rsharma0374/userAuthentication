package com.guardianservices.userAuthentication.client;

import com.guardianservices.userAuthentication.fallback.EmailConnectorClientFallback;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "email-service",                       // Must match spring.application.name in Email Connector
        fallback = EmailConnectorClientFallback.class
)
public interface EmailConnectorClient   {


}
