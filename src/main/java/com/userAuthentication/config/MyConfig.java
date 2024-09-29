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
import java.util.Properties;

@Configuration
public class MyConfig {

    private static final String USER_AUTH_PROPERTIES_PATH = "/opt/configs/userAuth.properties";


    //    public static final long JWT_TOKEN_VALIDITY =  60;
    public static String userName = "";
    public static String rawPassword = "";
    public static final String USER_NAME = "USER_NAME";
    public static final String RAW_PASSWORD = "RAW_PASSWORD";

    static {
            Properties properties = ResponseUtility.fetchProperties(USER_AUTH_PROPERTIES_PATH);
            if (null != properties) {
                userName = properties.getProperty(USER_NAME);
                rawPassword = properties.getProperty(RAW_PASSWORD);
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
