package com.todoapp.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_tag_relationships", indexes = {
        @Index(name = "idx_task_id", columnList = "task_id"),
        @Index(name = "idx_tag_id", columnList = "tag_id")
})
@EntityListeners(AuditingEntityListener.class)
@IdClass(TaskTagRelationship.TaskTagId.class)
@Getter
@Setter
@NoArgsConstructor
public class TaskTagRelationship {

    @Id
    @NotNull(message = "Task ID is required")
    @Column(name = "task_id")
    private Long taskId;

    @Id
    @NotNull(message = "Tag ID is required")
    @Column(name = "tag_id")
    private Long tagId;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private TaskTag tag;

    // Constructors
    public TaskTagRelationship(Long taskId, Long tagId) {
        this.taskId = taskId;
        this.tagId = tagId;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class TaskTagId implements Serializable {
        // Getters and Setters
        private Long taskId;
        private Long tagId;

        public TaskTagId(Long taskId, Long tagId) {
            this.taskId = taskId;
            this.tagId = tagId;
        }
    }
}