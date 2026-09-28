package com.guardianservices.userauthentication;

import com.guardianservices.userauthentication.authentication.service.JwtService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import com.guardianservices.userauthentication.platform.config.AuthProperties;
import com.guardianservices.userauthentication.platform.config.StorageProperties;
import com.guardianservices.userauthentication.platform.config.NotificationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.kms.KmsClient;

@SpringBootApplication(scanBasePackages = {"com.guardianservices.userauthentication"})
@EnableConfigurationProperties({AuthProperties.class, StorageProperties.class, NotificationProperties.class})
public class UserAuthenticationApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
        return applicationBuilder.sources(UserAuthenticationApplication.class);
    }

    public static void main(String[] args) {
        System.setProperty("spring.threads.virtual.enabled", "true");
        SpringApplication.run(UserAuthenticationApplication.class, args);
    }

    @Bean
    public S3Client s3Client(StorageProperties storageProperties) {
        return S3Client.builder()
            .region(Region.of(storageProperties.getS3().getRegion()))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .endpointOverride(storageProperties.getS3().getEndpointOverride() != null 
                ? java.net.URI.create(storageProperties.getS3().getEndpointOverride()) 
                : null)
            .build();
    }

    @Bean
    public S3Presigner s3Presigner(StorageProperties storageProperties) {
        return S3Presigner.builder()
            .region(Region.of(storageProperties.getS3().getRegion()))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .endpointOverride(storageProperties.getS3().getEndpointOverride() != null 
                ? java.net.URI.create(storageProperties.getS3().getEndpointOverride()) 
                : null)
            .build();
    }

    @Bean
    public SqsClient sqsClient(StorageProperties storageProperties) {
        return SqsClient.builder()
            .region(Region.of(storageProperties.getS3().getRegion()))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    @Bean
    public KmsClient kmsClient(StorageProperties storageProperties) {
        return KmsClient.builder()
            .region(Region.of(storageProperties.getS3().getRegion()))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    @Bean
    public JwtService jwtService(AuthProperties authProperties,
                                  com.guardianservices.userauthentication.account.repository.UserRoleRepository userRoleRepository) {
        JwtService jwtService = new JwtService(authProperties, userRoleRepository);
        jwtService.initialize();
        return jwtService;
    }
}