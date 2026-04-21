package com.wellbeing.model;

import java.time.LocalDateTime;

public class AppDetail {
    private final String appName;
    private final long timeUsedTodaySeconds;
    private final LocalDateTime lastUsed;

    public AppDetail(String appName, long timeUsedTodaySeconds, LocalDateTime lastUsed) {
        this.appName = appName;
        this.timeUsedTodaySeconds = timeUsedTodaySeconds;
        this.lastUsed = lastUsed;
    }

    public String getAppName() {
        return appName;
    }

    public long getTimeUsedTodaySeconds() {
        return timeUsedTodaySeconds;
    }

    public LocalDateTime getLastUsed() {
        return lastUsed;
    }
}
