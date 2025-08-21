package com.todoapp.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Disable CSRF for API Gateway
                .csrf(csrf -> csrf.disable())

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Disable form login and basic auth
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Stateless session management
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // Configure authorization
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - allow all (updated paths)
                        .pathMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/auth/health",
                                "/api/tasks/health",
                                "/health",
                                "/actuator/**"
                        ).permitAll()

                        // All other requests require authentication
                        .anyExchange().authenticated()
                )

                .build();
    }
}