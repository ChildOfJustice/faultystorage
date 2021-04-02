package org.yuldashev.s3server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.validation.constraints.NotNull;

@Configuration
@SpringBootApplication
public class Application {
    public static void main(@NotNull final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @NotNull
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
