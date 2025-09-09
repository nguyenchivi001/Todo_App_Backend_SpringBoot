package com.todoapp.task.service;

import com.todoapp.task.dto.request.CategoryRequest;
import com.todoapp.task.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request, Long userId);

    List<CategoryResponse> getAllCategories(Long userId);

    CategoryResponse getCategoryById(Long categoryId, Long userId);

    CategoryResponse updateCategory(Long categoryId, CategoryRequest request, Long userId);

    void deleteCategory(Long categoryId, Long userId);

    void validateCategoryExists(Long categoryId, Long userId);
}
