package com.todoapp.task.service;

import com.todoapp.task.dto.*;
import com.todoapp.task.dto.request.TaskRequest;
import com.todoapp.task.dto.response.TaskResponse;
import com.todoapp.task.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(TaskRequest request, Long userId);

    Page<TaskResponse> getAllTasks(Long userId, Pageable pageable);

    TaskResponse getTaskById(Long taskId, Long userId);

    TaskResponse updateTask(Long taskId, TaskRequest request, Long userId);

    TaskResponse toggleCompletion(Long taskId, Long userId);

    void deleteTask(Long taskId, Long userId);

    Page<TaskResponse> getCompletedTasks(Long userId, Pageable pageable);

    Page<TaskResponse> getPendingTasks(Long userId, Pageable pageable);

    List<TaskResponse> getTasksByPriority(Long userId, Priority priority);

    List<TaskResponse> getOverdueTasks(Long userId);

    List<TaskResponse> getTasksDueToday(Long userId);

    List<TaskResponse> searchTasks(Long userId, String keyword);

    TaskStatisticsResponse getStatistics(Long userId);
}
