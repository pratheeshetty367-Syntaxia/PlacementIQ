package com.placementiq;
 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
 
/**
 * Entry point for the PlacementIQ backend application.
 * Bootstraps the Spring Boot context, component scanning, auto-configuration,
 * and starts the embedded Tomcat server.
 */
@SpringBootApplication
public class PlacementIqApplication {
 
    public static void main(String[] args) {
        SpringApplication.run(PlacementIqApplication.class, args);
    }
}
 
