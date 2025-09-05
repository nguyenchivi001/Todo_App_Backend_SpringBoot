package com.todoapp.task.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ActivityType {
    CREATED("CREATED", "Task Created", "Task was created"),
    UPDATED("UPDATED", "Task Updated", "Task details were updated"),
    COMPLETED("COMPLETED", "Task Completed", "Task was marked as completed"),
    REOPENED("REOPENED", "Task Reopened", "Task was reopened"),
    DELETED("DELETED", "Task Deleted", "Task was deleted"),
    COMMENTED("COMMENTED", "Comment Added", "A comment was added to the task"),
    ATTACHED("ATTACHED", "File Attached", "A file was attached to the task"),
    PRIORITY_CHANGED("PRIORITY_CHANGED", "Priority Changed", "Task priority was changed"),
    DUE_DATE_CHANGED("DUE_DATE_CHANGED", "Due Date Changed", "Task due date was changed"),
    CATEGORY_CHANGED("CATEGORY_CHANGED", "Category Changed", "Task category was changed"),
    TAG_ADDED("TAG_ADDED", "Tag Added", "A tag was added to the task"),
    TAG_REMOVED("TAG_REMOVED", "Tag Removed", "A tag was removed from the task");

    private final String value;
    @Getter
    private final String displayName;
    @Getter
    private final String description;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ActivityType fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (ActivityType type : ActivityType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid activity type value: " + value);
    }

    /**
     * Check if this activity type is a modification type
     */
    public boolean isModification() {
        return this == UPDATED ||
                this == PRIORITY_CHANGED ||
                this == DUE_DATE_CHANGED ||
                this == CATEGORY_CHANGED;
    }

    /**
     * Check if this activity type is a status change
     */
    public boolean isStatusChange() {
        return this == COMPLETED || this == REOPENED;
    }

    /**
     * Check if this activity type is content addition
     */
    public boolean isContentAddition() {
        return this == COMMENTED || this == ATTACHED || this == TAG_ADDED;
    }

    /**
     * Check if this activity type is content removal
     */
    public boolean isContentRemoval() {
        return this == DELETED || this == TAG_REMOVED;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
