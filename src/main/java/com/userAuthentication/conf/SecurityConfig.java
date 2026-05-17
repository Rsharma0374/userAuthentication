package com.userAuthentication.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Simplified security configuration for UAM service
 * Gateway handles authentication, UAM trusts internal requests
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure security for internal service-to-service communication
     * Trust all internal traffic from API Gateway
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests(authz -> authz
                        // Internal health checks
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Internal token validation endpoint for gateway
                        .requestMatchers("/internal/token/validate").permitAll()
                        // All other endpoints require internal service authentication
                        .anyRequest().authenticated()
                )
                // Trust internal API Gateway (in production, use client certificate or JWT)
                .oauth2ResourceServer().jwt();

        return http.build();
    }
}
