package com.guardianservices.userAuthentication.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class Helper {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static Properties fetchProperties(String passManagerPropertiesPath) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(passManagerPropertiesPath));
            return properties;
        } catch (IOException e) {
            log.error("Exception occurred while getting pass manager config with probable cause - ", e);
            return null;
        }
    }

    public static List<String> extractRoles(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null || realmAccess.get("roles") == null) {
            return Collections.emptyList();
        }

        return (List<String>) realmAccess.get("roles");
    }
}
