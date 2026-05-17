package com.userAuthentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.userAuthentication"})
public class UserAuthenticationApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
		return applicationBuilder.sources(UserAuthenticationApplication.class);
	}

	public static void main(String[] args) {
		System.setProperty("spring.threads.virtual.enabled", "true");
		SpringApplication.run(UserAuthenticationApplication.class, args);
	}

}
