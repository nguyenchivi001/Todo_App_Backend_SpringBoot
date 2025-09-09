package com.todoapp.task.repository;

import com.todoapp.task.entity.Task;
import com.todoapp.task.enums.Priority;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Basic queries
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Task> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Task> findByIdAndUserId(Long id, Long userId);

    // Filter by completion status
    List<Task> findByUserIdAndCompleted(Long userId, Boolean completed);
    Page<Task> findByUserIdAndCompleted(Long userId, Boolean completed, Pageable pageable);

    // Filter by priority
    List<Task> findByUserIdAndPriority(Long userId, Priority priority);

    // Filter by category
    List<Task> findByUserIdAndCategoryId(Long userId, Long categoryId);

    // Due date queries
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.dueDate < :now AND t.completed = false")
    List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND DATE(t.dueDate) = CURRENT_DATE AND t.completed = false")
    List<Task> findTasksDueToday(@Param("userId") Long userId);

    // Search
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND (t.title LIKE %:keyword% OR t.description LIKE %:keyword%)")
    List<Task> searchTasks(@Param("userId") Long userId, @Param("keyword") String keyword);

    // Statistics
    long countByUserId(Long userId);
    long countByUserIdAndCompleted(Long userId, Boolean completed);
    long countByUserIdAndPriority(Long userId, Priority priority);

    // Delete
    void deleteByIdAndUserId(Long id, Long userId);
}

