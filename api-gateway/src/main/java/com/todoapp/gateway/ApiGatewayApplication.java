package com.todoapp.gateway;

import com.todoapp.gateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class ApiGatewayApplication {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${service.auth-service.url}")
    private String authServiceUrl;

    @Value("${service.task-service.url}")
    private String taskServiceUrl;

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ===========================================
                // AUTH SERVICE ROUTES
                // ===========================================

                // Public Auth Endpoints - No JWT required
                .route("auth-login", r -> r
                        .path("/api/auth/login")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addResponseHeader("X-Gateway-Response", "auth-service")
                                .circuitBreaker(c -> c
                                        .setName("auth-login-cb")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri(authServiceUrl))

                .route("auth-register", r -> r
                        .path("/api/auth/register")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addResponseHeader("X-Gateway-Response", "auth-service")
                                .circuitBreaker(c -> c
                                        .setName("auth-register-cb")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri(authServiceUrl))

                .route("auth-refresh", r -> r
                        .path("/api/auth/refresh")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addResponseHeader("X-Gateway-Response", "auth-service")
                                .circuitBreaker(c -> c
                                        .setName("auth-refresh-cb")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri(authServiceUrl))

                // Protected Auth Endpoints - JWT required
                .route("auth-profile", r -> r
                        .path("/api/auth/profile", "/api/auth/logout")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter)
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addResponseHeader("X-Gateway-Response", "auth-service")
                                .circuitBreaker(c -> c
                                        .setName("auth-protected-cb")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri(authServiceUrl))

                // ===========================================
                // TASK SERVICE ROUTES - All Protected
                // ===========================================
                .route("task-service", r -> r
                        .path("/api/tasks/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter)
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addResponseHeader("X-Gateway-Response", "task-service")
                                .circuitBreaker(c -> c
                                        .setName("task-service-cb")
                                        .setFallbackUri("forward:/fallback/task"))
                                .requestRateLimiter(r1 -> r1
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(exchange ->
                                                reactor.core.publisher.Mono.just(
                                                        exchange.getRequest().getHeaders().getFirst("X-User-Id") != null ?
                                                                exchange.getRequest().getHeaders().getFirst("X-User-Id") :
                                                                "anonymous"
                                                )))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                                        .setBackoff(Duration.ofSeconds(1), Duration.ofSeconds(5), 2, false)))
                        .uri(taskServiceUrl))

                // ===========================================
                // HEALTH CHECK & FALLBACK ROUTES
                // ===========================================
                .route("gateway-health", r -> r
                        .path("/health")
                        .filters(f -> f.addResponseHeader("Service", "api-gateway"))
                        .uri("http://localhost:8080"))

                .route("auth-health", r -> r
                        .path("/api/auth/health")
                        .filters(f -> f.stripPrefix(2))
                        .uri(authServiceUrl))

                .route("task-health", r -> r
                        .path("/api/tasks/health")
                        .filters(f -> f.stripPrefix(2))
                        .uri(taskServiceUrl))

                .build();
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        // 10 requests per second, with burst of 20
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20, 1);
    }
}