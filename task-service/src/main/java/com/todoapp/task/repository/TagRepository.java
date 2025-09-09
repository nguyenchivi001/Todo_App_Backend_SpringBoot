package com.todoapp.task.repository;

import com.todoapp.task.entity.TaskTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TaskTag, Long> {

    List<TaskTag> findByUserIdOrderByNameAsc(Long userId);
    Optional<TaskTag> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
    List<TaskTag> findByIdInAndUserId(List<Long> tagIds, Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}