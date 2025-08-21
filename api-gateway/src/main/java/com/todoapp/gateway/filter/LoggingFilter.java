package com.todoapp.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Instant startTime = Instant.now();

        // Log incoming request
        logRequest(request);

        return chain.filter(exchange)
                .doOnTerminate(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    Instant endTime = Instant.now();
                    Duration duration = Duration.between(startTime, endTime);

                    // Log response
                    logResponse(request, response, duration);
                })
                .doOnError(throwable -> {
                    Instant endTime = Instant.now();
                    Duration duration = Duration.between(startTime, endTime);

                    // Log error
                    logError(request, throwable, duration);
                });
    }

    private void logRequest(ServerHttpRequest request) {
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String remoteAddress = getClientIpAddress(request);
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String userId = request.getHeaders().getFirst("X-User-Id");

        logger.info("→ {} {} {} - IP: {} - User: {} - UA: {}",
                method, path, query != null ? "?" + query : "",
                remoteAddress, userId != null ? userId : "anonymous",
                userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "unknown");

        // Log headers (excluding sensitive ones) - only in debug mode
        if (logger.isDebugEnabled()) {
            request.getHeaders().forEach((key, value) -> {
                if (!isSensitiveHeader(key)) {
                    logger.debug("Request Header: {}: {}", key, String.join(", ", value));
                }
            });
        }
    }

    private void logResponse(ServerHttpRequest request, ServerHttpResponse response, Duration duration) {
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : -1;
        String gatewayResponse = response.getHeaders().getFirst("X-Gateway-Response");

        logger.info("← {} {} - Status: {} - Service: {} - Duration: {}ms",
                method, path, statusCode, gatewayResponse != null ? gatewayResponse : "unknown", duration.toMillis());

        // Log response headers (excluding sensitive ones) - only in debug mode
        if (logger.isDebugEnabled()) {
            response.getHeaders().forEach((key, value) -> {
                if (!isSensitiveHeader(key)) {
                    logger.debug("Response Header: {}: {}", key, String.join(", ", value));
                }
            });
        }
    }

    private void logError(ServerHttpRequest request, Throwable throwable, Duration duration) {
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String userId = request.getHeaders().getFirst("X-User-Id");

        logger.error("✗ {} {} - User: {} - Duration: {}ms - Error: {}",
                method, path, userId != null ? userId : "anonymous",
                duration.toMillis(), throwable.getMessage(), throwable);
    }

    private String getClientIpAddress(ServerHttpRequest request) {
        // Check X-Forwarded-For header first (from load balancer)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header (from nginx)
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        // Check X-Forwarded-Host
        String xForwardedHost = request.getHeaders().getFirst("X-Forwarded-Host");
        if (xForwardedHost != null && !xForwardedHost.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedHost)) {
            return xForwardedHost;
        }

        // Fallback to remote address
        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() :
                "unknown";
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseHeader = headerName.toLowerCase();
        return lowerCaseHeader.contains("authorization") ||
                lowerCaseHeader.contains("cookie") ||
                lowerCaseHeader.contains("password") ||
                lowerCaseHeader.contains("token") ||
                lowerCaseHeader.contains("secret") ||
                lowerCaseHeader.contains("key");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}