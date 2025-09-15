package com.todoapp.task.controller;

import com.todoapp.task.dto.request.CategoryRequest;
import com.todoapp.task.dto.response.ApiResponse;
import com.todoapp.task.dto.response.CategoryResponse;
import com.todoapp.task.security.UserPrincipal;
import com.todoapp.task.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        CategoryResponse category = categoryService.createCategory(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @AuthenticationPrincipal UserPrincipal user) {

        List<CategoryResponse> categories = categoryService.getAllCategories(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserPrincipal user) {

        CategoryResponse category = categoryService.getCategoryById(categoryId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal user) {

        CategoryResponse category = categoryService.updateCategory(categoryId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserPrincipal user) {

        categoryService.deleteCategory(categoryId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}