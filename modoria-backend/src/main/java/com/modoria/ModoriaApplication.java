package com.modoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Modoria - Intelligent & Immersive Seasonal E-commerce Platform
 * 
 * Main entry point for the Spring Boot application.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class ModoriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModoriaApplication.class, args);
    }

}
