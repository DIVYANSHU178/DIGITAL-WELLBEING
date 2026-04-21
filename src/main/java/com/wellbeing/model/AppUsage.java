package com.wellbeing.model;

import java.time.LocalDateTime;

public class AppUsage {
    private int id;
    private String appName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;

    public AppUsage(String appName, LocalDateTime startTime, LocalDateTime endTime, long durationSeconds) {
        this.appName = appName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = durationSeconds;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
}
