package com.todoapp.task.service.impl;

import com.todoapp.task.dto.*;
import com.todoapp.task.dto.request.TaskRequest;
import com.todoapp.task.dto.response.TaskResponse;
import com.todoapp.task.dto.response.TaskStatisticsResponse;
import com.todoapp.task.entity.*;
import com.todoapp.task.enums.Priority;
import com.todoapp.task.exception.TaskNotFoundException;
import com.todoapp.task.repository.TaskRepository;
import com.todoapp.task.service.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CategoryService categoryService;
    private final TagService tagService;

    public TaskServiceImpl(TaskRepository taskRepository, CategoryService categoryService, TagService tagService) {
        this.taskRepository = taskRepository;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    @Override
    public TaskResponse createTask(TaskRequest request, Long userId) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(false);
        task.setUserId(userId);
        task.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        task.setDueDate(request.getDueDate());

        if (request.getCategoryId() != null) {
            categoryService.validateCategoryExists(request.getCategoryId(), userId);
            task.setCategoryId(request.getCategoryId());
        }

        task = taskRepository.save(task);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tagService.addTagsToTask(task.getId(), request.getTagIds(), userId);
        }

        return convertToResponse(task);
    }

    @Override
    @Transactional
    public Page<TaskResponse> getAllTasks(Long userId, Pageable pageable) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional
    public TaskResponse getTaskById(Long taskId, Long userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        return convertToResponse(task);
    }

    @Override
    public TaskResponse updateTask(Long taskId, TaskRequest request, Long userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        if (request.getCompleted() != null) {
            task.setCompleted(request.getCompleted());
        }

        if (request.getCategoryId() != null) {
            categoryService.validateCategoryExists(request.getCategoryId(), userId);
            task.setCategoryId(request.getCategoryId());
        } else {
            task.setCategoryId(null);
        }

        task = taskRepository.save(task);

        if (request.getTagIds() != null) {
            tagService.updateTaskTags(task.getId(), request.getTagIds(), userId);
        }

        return convertToResponse(task);
    }

    @Override
    public TaskResponse toggleCompletion(Long taskId, Long userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        task.setCompleted(!task.getCompleted());
        task = taskRepository.save(task);

        return convertToResponse(task);
    }

    @Override
    public void deleteTask(Long taskId, Long userId) {
        if (!taskRepository.findByIdAndUserId(taskId, userId).isPresent()) {
            throw new TaskNotFoundException("Task not found");
        }
        taskRepository.deleteByIdAndUserId(taskId, userId);
    }

    @Override
    @Transactional
    public Page<TaskResponse> getCompletedTasks(Long userId, Pageable pageable) {
        return taskRepository.findByUserIdAndCompleted(userId, true, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional
    public Page<TaskResponse> getPendingTasks(Long userId, Pageable pageable) {
        return taskRepository.findByUserIdAndCompleted(userId, false, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional
    public List<TaskResponse> getTasksByPriority(Long userId, Priority priority) {
        return taskRepository.findByUserIdAndPriority(userId, priority)
                .stream().map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TaskResponse> getOverdueTasks(Long userId) {
        return taskRepository.findOverdueTasks(userId, LocalDateTime.now())
                .stream().map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TaskResponse> getTasksDueToday(Long userId) {
        return taskRepository.findTasksDueToday(userId)
                .stream().map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TaskResponse> searchTasks(Long userId, String keyword) {
        return taskRepository.searchTasks(userId, keyword)
                .stream().map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskStatisticsResponse getStatistics(Long userId) {
        TaskStatisticsResponse stats = new TaskStatisticsResponse();

        stats.setTotalTasks(taskRepository.countByUserId(userId));
        stats.setCompletedTasks(taskRepository.countByUserIdAndCompleted(userId, true));
        stats.setPendingTasks(taskRepository.countByUserIdAndCompleted(userId, false));

        stats.setHighPriorityTasks(taskRepository.countByUserIdAndPriority(userId, Priority.HIGH));
        stats.setMediumPriorityTasks(taskRepository.countByUserIdAndPriority(userId, Priority.MEDIUM));
        stats.setLowPriorityTasks(taskRepository.countByUserIdAndPriority(userId, Priority.LOW));

        if (stats.getTotalTasks() > 0) {
            double rate = (stats.getCompletedTasks().doubleValue() / stats.getTotalTasks().doubleValue()) * 100;
            stats.setCompletionRate(Math.round(rate * 100.0) / 100.0);
        } else {
            stats.setCompletionRate(0.0);
        }

        stats.setTasksToday((long) getTasksDueToday(userId).size());
        stats.setOverdueTasks((long) getOverdueTasks(userId).size());

        return stats;
    }

    private TaskResponse convertToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setCompleted(task.getCompleted());
        response.setUserId(task.getUserId());
        response.setDueDate(task.getDueDate());
        response.setPriority(task.getPriority());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        if (task.getCategoryId() != null) {
            try {
                response.setCategory(categoryService.getCategoryById(task.getCategoryId(), task.getUserId()));
            } catch (Exception e) {
                // ignore deleted category
            }
        }

        response.setTags(tagService.getTagsByTaskId(task.getId()));

        return response;
    }
}