package com.todoapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.todoapp.auth.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Boolean enabled;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("last_login")
    private LocalDateTime lastLogin;

    // Constructors
    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.fullName = user.getFullName();
        this.enabled = user.isEnabled();
        this.createdAt = user.getCreatedAt();
        this.lastLogin = user.getLastLogin();
    }

    // Static factory method
    public static UserDto fromUser(User user) {
        return new UserDto(user);
    }
}