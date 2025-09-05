package com.todoapp.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "task_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_user_tag_name", columnNames = {"user_id", "name"})
        },
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_name", columnList = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TaskTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tag name is required")
    @Size(max = 50, message = "Tag name must be less than 50 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "tagId",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private Set<TaskTagRelationship> taskTagRelationships = new HashSet<>();

    //Contructor with parameters
    public TaskTag(String name, Long userId) {
        this.name = name;
        this.userId = userId;
    }
}
