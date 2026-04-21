package com.wellbeing.model;

import java.time.LocalDate;

public class DailyUsage {
    private final LocalDate date;
    private final String dayLabel;
    private long durationSeconds;

    public DailyUsage(LocalDate date, String dayLabel, long durationSeconds) {
        this.date = date;
        this.dayLabel = dayLabel;
        this.durationSeconds = durationSeconds;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void addDurationSeconds(long additionalSeconds) {
        durationSeconds += additionalSeconds;
    }
}
