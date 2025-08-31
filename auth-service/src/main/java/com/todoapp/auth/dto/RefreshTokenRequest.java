package com.todoapp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    // Constructors
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}