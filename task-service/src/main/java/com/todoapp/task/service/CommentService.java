package com.todoapp.task.service;

import com.todoapp.task.dto.request.CommentRequest;
import com.todoapp.task.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse addComment(Long taskId, CommentRequest request, Long userId);

    List<CommentResponse> getCommentsByTask(Long taskId, Long userId);

    void deleteComment(Long commentId, Long userId);
}
