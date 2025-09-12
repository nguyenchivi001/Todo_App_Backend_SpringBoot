package com.todoapp.task.util;

public class Constants {

    // API Response Messages
    public static final String SUCCESS = "Success";
    public static final String TASK_CREATED = "Task created successfully";
    public static final String TASK_UPDATED = "Task updated successfully";
    public static final String TASK_DELETED = "Task deleted successfully";
    public static final String TASK_NOT_FOUND = "Task not found";

    public static final String CATEGORY_CREATED = "Category created successfully";
    public static final String CATEGORY_UPDATED = "Category updated successfully";
    public static final String CATEGORY_DELETED = "Category deleted successfully";
    public static final String CATEGORY_NOT_FOUND = "Category not found";

    public static final String TAG_CREATED = "Tag created successfully";
    public static final String TAG_DELETED = "Tag deleted successfully";
    public static final String TAG_NOT_FOUND = "Tag not found";

    public static final String COMMENT_CREATED = "Comment added successfully";
    public static final String COMMENT_DELETED = "Comment deleted successfully";
    public static final String COMMENT_NOT_FOUND = "Comment not found";

    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String INTERNAL_ERROR = "Internal server error";

    // Default Values
    public static final String DEFAULT_CATEGORY_COLOR = "#007bff";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Validation
    public static final int MAX_TITLE_LENGTH = 255;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_CATEGORY_NAME_LENGTH = 100;
    public static final int MAX_TAG_NAME_LENGTH = 50;
    public static final int MAX_COMMENT_LENGTH = 500;
}
