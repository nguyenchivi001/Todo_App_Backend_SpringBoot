package com.todoapp.task.repository;

import com.todoapp.task.entity.TaskCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<TaskCategory, Long> {

    List<TaskCategory> findByUserIdOrderByNameAsc(Long userId);
    Optional<TaskCategory> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
    void deleteByIdAndUserId(Long id, Long userId);
}