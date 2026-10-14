package com.roles.usermanagement.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuration to allow CORS (Cross-Origin Resource Sharing) requests.
 */
@Configuration
public class CorsConfig {

    /**
     * Defines the permitted CORS configuration for the application.
     *
     * @return CORS configuration source.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        // CORS configuration
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Allow all requests from any origin (can be restricted to specific origins here)
        corsConfiguration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "http://localhost:5501",
            "http://127.0.0.1:5501"
        )); // Can be changed to a list of allowed origins e.g.: http://localhost:4200

        // Allow specific HTTP methods (GET, POST, PUT, PATCH, DELETE, OPTIONS)
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow all headers in requests
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));

        // URL-based CORS configuration
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}
