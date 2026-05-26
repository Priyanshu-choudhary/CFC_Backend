package com.cfc.platform.ConfigSecurity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Creates the AWS SQS v2 client used by ExecutionJobService to publish
 * code-execution jobs to the cfc-execution-jobs queue.
 *
 * Credentials:
 *   DefaultCredentialsProvider automatically picks up the ECS task IAM role
 *   (ecsTaskRole-cfc-backend) at runtime — no keys in env vars needed.
 *   Locally it falls back to ~/.aws/credentials.
 *
 * The ecsTaskRole-cfc-backend must have:
 *   sqs:SendMessage on arn:aws:sqs:ap-south-1:936344984906:cfc-execution-jobs
 */
@Configuration
public class SqsConfig {

    // Reuses the same region property as S3 — both are in ap-south-1
    @Value("${aws.s3.region:ap-south-1}")
    private String region;

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
