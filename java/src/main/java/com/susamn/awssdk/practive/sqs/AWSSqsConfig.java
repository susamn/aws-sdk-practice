package com.susamn.awssdk.practive.sqs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.util.Objects;

/**
 * @author Supratim Samanta
 * @date 5/3/21 at 11:21
 */
@Configuration
public class AWSSqsConfig {

    @Bean
    public SqsAsyncClient amazonSQSAsyncClient() {
        Region region = Objects.requireNonNull(Region.of(System.getenv("AWS_REGION")));
        return SqsAsyncClient.builder()
                .region(region)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create()).build();
    }
}