package com.wellbeing.model;

import java.time.LocalDateTime;

public class AppStats {
    private final String appName;
    private long totalDurationSeconds;
    private LocalDateTime lastUsed;

    public AppStats(String appName, long totalDurationSeconds) {
        this(appName, totalDurationSeconds, null);
    }

    public AppStats(String appName, long totalDurationSeconds, LocalDateTime lastUsed) {
        this.appName = appName;
        this.totalDurationSeconds = totalDurationSeconds;
        this.lastUsed = lastUsed;
    }

    public String getAppName() {
        return appName;
    }

    public long getTotalDurationSeconds() {
        return totalDurationSeconds;
    }

    public LocalDateTime getLastUsed() {
        return lastUsed;
    }

    public void addDurationSeconds(long additionalSeconds) {
        totalDurationSeconds += additionalSeconds;
    }

    public void updateLastUsed(LocalDateTime candidate) {
        if (candidate == null) {
            return;
        }
        if (lastUsed == null || candidate.isAfter(lastUsed)) {
            lastUsed = candidate;
        }
    }
}
