package com.todoapp.gateway;

import com.todoapp.gateway.filter.JwtAuthenticationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes - Public endpoints
                .route("auth-login", r -> r.path("/api/auth/login")
                        .uri("http://auth-service:8081"))
                .route("auth-register", r -> r.path("/api/auth/register")
                        .uri("http://auth-service:8081"))
                .route("auth-refresh", r -> r.path("/api/auth/refresh")
                        .uri("http://auth-service:8081"))

                // Auth Service Routes - Protected endpoints
                .route("auth-profile", r -> r.path("/api/auth/profile")
                        .filters(f -> f.filter(new JwtAuthenticationFilter()))
                        .uri("http://auth-service:8081"))
                .route("auth-logout", r -> r.path("/api/auth/logout")
                        .filters(f -> f.filter(new JwtAuthenticationFilter()))
                        .uri("http://auth-service:8081"))

                // Task Service Routes - All protected
                .route("tasks", r -> r.path("/api/tasks/**")
                        .filters(f -> f.filter(new JwtAuthenticationFilter()))
                                .uri("http://task-service:8082"))

                // Health check routes
                .route("auth-health", r -> r.path("/api/auth/health")
                        .uri("http://auth-service:8081"))
                .route("task-health", r -> r.path("/api/tasks/health")
                        .uri("http://task-service:8082"))
                .build();


    }
}
