package com.chat.userAuthentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication(scanBasePackages = {"com.chat.userAuthentication"})
public class UserAuthenticationApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
		return applicationBuilder.sources(UserAuthenticationApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(UserAuthenticationApplication.class, args);
	}

}
