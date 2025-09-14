package com.student.quizapp;

import static org.mockito.Mockito.mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    public RestTemplate restTemplate() {
        return mock(RestTemplate.class); // Mock RestTemplate to avoid real API calls in tests
    }
}