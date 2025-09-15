package com.todoapp.task.controller;

import com.todoapp.task.dto.request.TaskRequest;
import com.todoapp.task.dto.response.ApiResponse;
import com.todoapp.task.dto.response.TaskResponse;
import com.todoapp.task.dto.response.TaskStatisticsResponse;
import com.todoapp.task.enums.Priority;
import com.todoapp.task.security.UserPrincipal;
import com.todoapp.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        TaskResponse task = taskService.createTask(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal user) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponse> tasks = taskService.getAllTasks(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal user) {

        TaskResponse task = taskService.getTaskById(taskId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", task));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        TaskResponse task = taskService.updateTask(taskId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    @PatchMapping("/{taskId}/toggle")
    public ResponseEntity<ApiResponse<TaskResponse>> toggleCompletion(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal user) {

        TaskResponse task = taskService.toggleCompletion(taskId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Task status updated", task));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal user) {

        taskService.deleteTask(taskId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getCompletedTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal user) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponse> tasks = taskService.getCompletedTasks(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Completed tasks retrieved", tasks));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getPendingTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal user) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponse> tasks = taskService.getPendingTasks(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending tasks retrieved", tasks));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByPriority(
            @PathVariable Priority priority,
            @AuthenticationPrincipal UserPrincipal user) {

        List<TaskResponse> tasks = taskService.getTasksByPriority(user.getId(), priority);
        return ResponseEntity.ok(ApiResponse.success("Tasks by priority retrieved", tasks));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks(
            @AuthenticationPrincipal UserPrincipal user) {

        List<TaskResponse> tasks = taskService.getOverdueTasks(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Overdue tasks retrieved", tasks));
    }

    @GetMapping("/due-today")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksDueToday(
            @AuthenticationPrincipal UserPrincipal user) {

        List<TaskResponse> tasks = taskService.getTasksDueToday(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Tasks due today retrieved", tasks));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> searchTasks(
            @RequestParam String keyword,
            @AuthenticationPrincipal UserPrincipal user) {

        List<TaskResponse> tasks = taskService.searchTasks(user.getId(), keyword);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", tasks));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<TaskStatisticsResponse>> getStatistics(
            @AuthenticationPrincipal UserPrincipal user) {

        TaskStatisticsResponse stats = taskService.getStatistics(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved", stats));
    }
}

