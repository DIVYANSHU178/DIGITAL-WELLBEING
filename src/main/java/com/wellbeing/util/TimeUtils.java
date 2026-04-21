package com.wellbeing.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    public static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");

    public static String formatDuration(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;

        if (h > 0) {
            return String.format("%dh %02dm %02ds", h, m, s);
        } else if (m > 0) {
            return String.format("%dm %02ds", m, s);
        } else {
            return String.format("%ds", s);
        }
    }

    public static String formatDurationShort(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0) {
            return String.format("%d hrs %02d mins", hours, minutes);
        }
        return String.format("%d mins", minutes);
    }

    public static String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Just now";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(dateTime, now).toMinutes();
        if (minutes < 1) {
            return "Just now";
        }
        if (minutes < 60) {
            return minutes + " min ago";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hr ago";
        }
        return dateTime.format(DISPLAY_DATE_TIME);
    }

    public static String formatClock(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "--";
        }
        return dateTime.format(DISPLAY_TIME);
    }
}
