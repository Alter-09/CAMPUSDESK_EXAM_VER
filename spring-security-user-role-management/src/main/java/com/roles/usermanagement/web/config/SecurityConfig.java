package com.roles.usermanagement.web.config;

import com.roles.usermanagement.domain.service.UserRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * Application security configuration. This class configures application security
 * by setting access permissions for different routes and defining authentication behavior.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Autowired
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Defines security configuration for routes and access restrictions.
     *
     * @param http The HttpSecurity object to configure security.
     * @return The security filter configured for the application.
     * @throws Exception If there are errors configuring security.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(customRequest -> customRequest
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/user/**").authenticated()
                /*.requestMatchers("/api/customers/**").hasAnyRole(UserRoles.Role.ADMIN.name(), UserRoles.Role.CUSTOMER.name())
                .requestMatchers(HttpMethod.GET, "/api/pizzas/**").hasAnyRole(UserRoles.Role.ADMIN.name(), UserRoles.Role.CUSTOMER.name())
                .requestMatchers(HttpMethod.POST, "/api/pizzas/**").hasRole(UserRoles.Role.ADMIN.name())
                .requestMatchers(HttpMethod.PUT).hasRole(UserRoles.Role.ADMIN.name())
                .requestMatchers("/api/orders/random").hasAuthority(UserRoles.Authority.RANDOM_ORDER.name())
                .requestMatchers("/api/orders/**").hasRole(UserRoles.Role.ADMIN.name())
                .requestMatchers(HttpMethod.GET,"/api/**").permitAll()
                .requestMatchers(HttpMethod.PUT).denyAll()*/
                .anyRequest().authenticated())
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    /**
     * Defines custom authentication manager used for user authentication.
     *
     * @param configuration Authentication configuration.
     * @return The custom authentication manager.
     * @throws Exception If there are errors retrieving the authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Returns the password encoder used for secure password storage.
     *
     * @return The password encoder used to encrypt and match passwords.
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
