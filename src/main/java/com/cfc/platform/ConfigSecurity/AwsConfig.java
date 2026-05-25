package com.cfc.platform.ConfigSecurity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Configuration
public class AwsConfig {

    @Value("${aws.s3.region:ap-south-1}")
    private String region;

    /**
     * Optional: set aws.s3.endpoint-override in application-local.properties
     * to point at LocalStack (http://localhost:4566) for offline development.
     * Leave blank (or omit) in docker/prod — it will hit real AWS.
     */
    @Value("${aws.s3.endpoint-override:}")
    private String endpointOverride;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                // On ECS this automatically uses the task's IAM role — no keys needed.
                // Locally it falls back to ~/.aws/credentials or env vars.
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride))
                   .forcePathStyle(true); // required for LocalStack
        }

        return builder.build();
    }
}
