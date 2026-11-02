package com.placementiq.config;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
 
import java.util.List;
 
/**
 * Defines the CORS policy for the application so that the ReactJS
 * frontend (running on a different origin/port) can call the REST APIs.
 */
@Configuration
public class CorsConfig {
 
    @Value("${placementiq.cors.allowed-origins}")
    private String allowedOrigins;
 
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        // MODIFIED IN PART 2: Content-Disposition must be exposed so the browser
        // (and the resume download helper in the React app) can read the original
        // file name returned by GET /api/resumes/me/download. Without this, the
        // header is hidden from JavaScript by the browser's CORS rules.
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
 
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
