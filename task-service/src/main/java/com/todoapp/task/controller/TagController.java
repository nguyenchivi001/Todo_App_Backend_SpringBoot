package com.todoapp.task.controller;

import com.todoapp.task.dto.request.TagRequest;
import com.todoapp.task.dto.response.ApiResponse;
import com.todoapp.task.dto.response.TagResponse;
import com.todoapp.task.security.UserPrincipal;
import com.todoapp.task.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        TagResponse tag = tagService.createTag(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tag created successfully", tag));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags(
            @AuthenticationPrincipal UserPrincipal user) {

        List<TagResponse> tags = tagService.getAllTags(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", tags));
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagById(
            @PathVariable Long tagId,
            @AuthenticationPrincipal UserPrincipal user) {

        TagResponse tag = tagService.getTagById(tagId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Tag retrieved successfully", tag));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable Long tagId,
            @AuthenticationPrincipal UserPrincipal user) {

        tagService.deleteTag(tagId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Tag deleted successfully", null));
    }
}
