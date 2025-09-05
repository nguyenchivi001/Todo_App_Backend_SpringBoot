package com.todoapp.task.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Priority {
    LOW("LOW", 1, "Low Priority"),
    MEDIUM("MEDIUM", 2, "Medium Priority"),
    HIGH("HIGH", 3, "High Priority");

    private final String value;
    private final int order;
    @Getter
    private final String displayName;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Priority fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (Priority priority : Priority.values()) {
            if (priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }

        throw new IllegalArgumentException("Invalid priority value: " + value);
    }

    /**
     * Get priority by order (for sorting purposes)
     */
    public static Priority fromOrder(int order) {
        for (Priority priority : Priority.values()) {
            if (priority.order == order) {
                return priority;
            }
        }
        return MEDIUM; // default
    }

    /**
     * Check if this priority is higher than another
     */
    public boolean isHigherThan(Priority other) {
        return this.order > other.order;
    }

    /**
     * Check if this priority is lower than another
     */
    public boolean isLowerThan(Priority other) {
        return this.order < other.order;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
