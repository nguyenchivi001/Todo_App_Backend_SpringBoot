package com.todoapp.task.controller;

import com.todoapp.task.dto.request.CommentRequest;
import com.todoapp.task.dto.response.ApiResponse;
import com.todoapp.task.dto.response.CommentResponse;
import com.todoapp.task.security.UserPrincipal;
import com.todoapp.task.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        CommentResponse comment = commentService.addComment(taskId, request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", comment));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal user) {

        List<CommentResponse> comments = commentService.getCommentsByTask(taskId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal user) {

        commentService.deleteComment(commentId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}