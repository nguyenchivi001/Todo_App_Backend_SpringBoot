package com.todoapp.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    @PostMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        return buildFallbackResponse(
                "Auth Service is temporarily unavailable",
                "AUTH_SERVICE_DOWN",
                "Please try again later or contact support if the problem persists"
        );
    }

    @GetMapping("/task")
    @PostMapping("/task")
    public ResponseEntity<Map<String, Object>> taskServiceFallback() {
        return buildFallbackResponse(
                "Task Service is temporarily unavailable",
                "TASK_SERVICE_DOWN",
                "Your tasks are safe. Please try again in a few moments"
        );
    }

    @GetMapping("/default")
    public ResponseEntity<Map<String, Object>> defaultFallback() {
        return buildFallbackResponse(
                "Service is temporarily unavailable",
                "SERVICE_DOWN",
                "Please try again later"
        );
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(
            String message, String errorCode, String userMessage) {

        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        response.put("errorCode", errorCode);
        response.put("userMessage", userMessage);
        response.put("timestamp", Instant.now());
        response.put("service", "api-gateway");
        response.put("type", "CIRCUIT_BREAKER_FALLBACK");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
