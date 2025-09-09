package com.todoapp.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequest {

    @NotBlank(message = "Comment content is required")
    private String comment;

    // Constructors
    public CommentRequest(String comment) {
        this.comment = comment;
    }
}