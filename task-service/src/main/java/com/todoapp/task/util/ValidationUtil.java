package com.todoapp.task.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

    public static boolean isValidTitle(String title) {
        return StringUtils.hasText(title) && title.trim().length() <= 255;
    }

    public static boolean isValidHexColor(String color) {
        return color != null && HEX_COLOR_PATTERN.matcher(color).matches();
    }

    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        return input.trim();
    }

    public static boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && email.contains("@");
    }
}

