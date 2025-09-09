package com.todoapp.task.service.impl;

import com.todoapp.task.dto.request.TagRequest;
import com.todoapp.task.dto.response.TagResponse;
import com.todoapp.task.entity.TaskTag;
import com.todoapp.task.entity.TaskTagRelationship;
import com.todoapp.task.exception.TaskNotFoundException;
import com.todoapp.task.repository.TagRepository;
import com.todoapp.task.service.TagService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public TagServiceImpl(TagRepository tagRepository, EntityManager entityManager) {
        this.tagRepository = tagRepository;
        this.entityManager = entityManager;
    }

    @Override
    public TagResponse createTag(TagRequest request, Long userId) {
        if (tagRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new IllegalArgumentException("Tag already exists");
        }

        TaskTag tag = new TaskTag();
        tag.setName(request.getName().toLowerCase());
        tag.setUserId(userId);

        tag = tagRepository.save(tag);
        return convertToResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags(Long userId) {
        return tagRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getTagById(Long tagId, Long userId) {
        TaskTag tag = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Tag not found"));
        return convertToResponse(tag);
    }

    @Override
    public void deleteTag(Long tagId, Long userId) {
        if (!tagRepository.findByIdAndUserId(tagId, userId).isPresent()) {
            throw new TaskNotFoundException("Tag not found");
        }
        tagRepository.deleteByIdAndUserId(tagId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsByTaskId(Long taskId) {
        String jpql = "SELECT t FROM TaskTag t JOIN TaskTagRelationship ttr ON t.id = ttr.tagId WHERE ttr.taskId = :taskId";
        List<TaskTag> tags = entityManager.createQuery(jpql, TaskTag.class)
                .setParameter("taskId", taskId)
                .getResultList();

        return tags.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public void addTagsToTask(Long taskId, List<Long> tagIds, Long userId) {
        List<TaskTag> tags = tagRepository.findByIdInAndUserId(tagIds, userId);
        if (tags.size() != tagIds.size()) {
            throw new TaskNotFoundException("Some tags not found");
        }

        for (Long tagId : tagIds) {
            TaskTagRelationship relationship = new TaskTagRelationship(taskId, tagId);
            entityManager.persist(relationship);
        }
    }

    @Override
    public void updateTaskTags(Long taskId, List<Long> newTagIds, Long userId) {
        String deleteJpql = "DELETE FROM TaskTagRelationship ttr WHERE ttr.taskId = :taskId";
        entityManager.createQuery(deleteJpql)
                .setParameter("taskId", taskId)
                .executeUpdate();

        if (newTagIds != null && !newTagIds.isEmpty()) {
            addTagsToTask(taskId, newTagIds, userId);
        }
    }

    // helper
    private TagResponse convertToResponse(TaskTag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setUserId(tag.getUserId());
        response.setCreatedAt(tag.getCreatedAt());
        return response;
    }
}
