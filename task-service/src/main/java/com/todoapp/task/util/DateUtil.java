package com.todoapp.task.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, DEFAULT_FORMATTER);
    }

    public static boolean isOverdue(LocalDateTime dueDate) {
        return dueDate != null && dueDate.isBefore(LocalDateTime.now());
    }

    public static boolean isDueToday(LocalDateTime dueDate) {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dueDate.toLocalDate().equals(now.toLocalDate());
    }

    public static long getDaysUntilDue(LocalDateTime dueDate) {
        if (dueDate == null) return 0;
        LocalDateTime now = LocalDateTime.now();
        return java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), dueDate.toLocalDate());
    }
}

