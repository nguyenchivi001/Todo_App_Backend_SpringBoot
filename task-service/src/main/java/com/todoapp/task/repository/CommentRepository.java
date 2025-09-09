package com.todoapp.task.repository;

import com.todoapp.task.entity.TaskComment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<TaskComment, Long> {

    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    @Query("SELECT c FROM TaskComment c JOIN Task t ON c.taskId = t.id WHERE c.id = :commentId AND t.userId = :userId")
    Optional<TaskComment> findByIdAndTaskUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    long countByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}