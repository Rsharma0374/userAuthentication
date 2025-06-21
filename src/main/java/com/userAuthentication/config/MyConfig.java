package com.userAuthentication.config;

import com.userAuthentication.utility.ResponseUtility;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Map;
import java.util.Properties;

@Configuration
public class MyConfig {



    public static String userName = "";
    public static String rawPassword = "";
    public static final String USER_NAME = "USER_NAME";
    public static final String RAW_PASSWORD = "RAW_PASSWORD";

    static {
        try {
            Map config = InfisicalConfig.fetchConfig("UserAuthConfig");
            if (config != null) {
                userName = (String) config.get(USER_NAME);
                rawPassword = (String) config.get(RAW_PASSWORD);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.builder().
                username(userName)
                .password(passwordEncoder().encode(rawPassword)).roles("ADMIN").
                        build();
        return new InMemoryUserDetailsManager(userDetails);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
