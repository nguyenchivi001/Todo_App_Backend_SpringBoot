package com.todoapp.task.service.impl;

import com.todoapp.task.dto.request.CategoryRequest;
import com.todoapp.task.dto.response.CategoryResponse;
import com.todoapp.task.entity.TaskCategory;
import com.todoapp.task.exception.CategoryNotFoundException;
import com.todoapp.task.repository.CategoryRepository;
import com.todoapp.task.service.CategoryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request, Long userId) {
        if (categoryRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new IllegalArgumentException("Category already exists");
        }

        TaskCategory category = new TaskCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());
        category.setUserId(userId);

        category = categoryRepository.save(category);
        return convertToResponse(category);
    }

    @Override
    @Transactional
    public List<CategoryResponse> getAllCategories(Long userId) {
        return categoryRepository.findByUserIdOrderByNameAsc(userId)
                .stream().map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse getCategoryById(Long categoryId, Long userId) {
        TaskCategory category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        return convertToResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request, Long userId) {
        TaskCategory category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());

        category = categoryRepository.save(category);
        return convertToResponse(category);
    }

    @Override
    public void deleteCategory(Long categoryId, Long userId) {
        if (!categoryRepository.findByIdAndUserId(categoryId, userId).isPresent()) {
            throw new CategoryNotFoundException("Category not found");
        }
        categoryRepository.deleteByIdAndUserId(categoryId, userId);
    }

    @Override
    public void validateCategoryExists(Long categoryId, Long userId) {
        if (!categoryRepository.findByIdAndUserId(categoryId, userId).isPresent()) {
            throw new CategoryNotFoundException("Category not found");
        }
    }

    private CategoryResponse convertToResponse(TaskCategory category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setColor(category.getColor());
        response.setUserId(category.getUserId());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}
