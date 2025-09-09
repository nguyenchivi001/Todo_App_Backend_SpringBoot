package com.todoapp.task.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.todoapp.task.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private Boolean completed;

    private Long userId;

    private CategoryResponse category;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;

    private Priority priority;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String status;

    private List<TagResponse> tags;

    private Integer commentCount;

    private Integer attachmentCount;

    private Boolean overdue;

    // Helper method to determine task status
    public void determineStatus() {
        if (completed) {
            this.status = "Completed";
        } else if (dueDate == null) {
            this.status = "No Due Date";
        } else if (dueDate.isBefore(LocalDateTime.now())) {
            this.status = "Overdue";
            this.overdue = true;
        } else if (dueDate.isBefore(LocalDateTime.now().plusDays(7))) {
            this.status = "Due Soon";
        } else {
            this.status = "On Track";
        }

        if (this.overdue == null) {
            this.overdue = false;
        }
    }
}