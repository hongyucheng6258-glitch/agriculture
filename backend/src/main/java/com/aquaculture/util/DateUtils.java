package com.aquaculture.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }

    public static String format(String dateTime) {
        return dateTime;
    }

    public static String parse(String dateTimeStr) {
        return dateTimeStr;
    }

    public static String getTodayStart() {
        return LocalDateTime.now().toLocalDate().atStartOfDay().format(FORMATTER);
    }

    public static String getDaysAgo(int days) {
        return LocalDateTime.now().minusDays(days).format(FORMATTER);
    }

    public static String getDayStart(int daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo).toLocalDate().atStartOfDay().format(FORMATTER);
    }

    public static String getDayEnd(int daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo).toLocalDate().atTime(23, 59, 59).format(FORMATTER);
    }
}
