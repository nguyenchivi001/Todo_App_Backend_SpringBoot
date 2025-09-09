package com.todoapp.task.service.impl;

import com.todoapp.task.dto.request.CommentRequest;
import com.todoapp.task.dto.response.CommentResponse;
import com.todoapp.task.entity.TaskComment;
import com.todoapp.task.exception.TaskNotFoundException;
import com.todoapp.task.repository.CommentRepository;
import com.todoapp.task.repository.TaskRepository;
import com.todoapp.task.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    public CommentServiceImpl(CommentRepository commentRepository, TaskRepository taskRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public CommentResponse addComment(Long taskId, CommentRequest request, Long userId) {
        // Validate task ownership
        if (taskRepository.findByIdAndUserId(taskId, userId).isEmpty()) {
            throw new TaskNotFoundException("Task not found");
        }

        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setComment(request.getComment());

        comment = commentRepository.save(comment);
        return convertToResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTask(Long taskId, Long userId) {
        // Validate task ownership
        if (!taskRepository.findByIdAndUserId(taskId, userId).isPresent()) {
            throw new TaskNotFoundException("Task not found");
        }

        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        TaskComment comment = commentRepository.findByIdAndTaskUserId(commentId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Comment not found"));
        commentRepository.delete(comment);
    }

    // helper
    private CommentResponse convertToResponse(TaskComment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setTaskId(comment.getTaskId());
        response.setUserId(comment.getUserId());
        response.setComment(comment.getComment());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
}
