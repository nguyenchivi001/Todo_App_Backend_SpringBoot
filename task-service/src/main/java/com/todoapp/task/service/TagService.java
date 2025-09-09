package com.todoapp.task.service;

import com.todoapp.task.dto.request.TagRequest;
import com.todoapp.task.dto.response.TagResponse;

import java.util.List;

public interface TagService {
    TagResponse createTag(TagRequest request, Long userId);

    List<TagResponse> getAllTags(Long userId);

    TagResponse getTagById(Long tagId, Long userId);

    void deleteTag(Long tagId, Long userId);

    List<TagResponse> getTagsByTaskId(Long taskId);

    void addTagsToTask(Long taskId, List<Long> tagIds, Long userId);

    void updateTaskTags(Long taskId, List<Long> newTagIds, Long userId);
}
