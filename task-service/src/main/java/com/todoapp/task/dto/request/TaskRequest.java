package com.todoapp.task.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.todoapp.task.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private String description;

    private Boolean completed = false;

    private Long categoryId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;

    private Priority priority = Priority.MEDIUM;

    private List<Long> tagIds;

    // Constructors
    public TaskRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }
}