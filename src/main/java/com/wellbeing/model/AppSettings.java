package com.wellbeing.model;

public class AppSettings {
    private final int dailyLimitHours;
    private final boolean notificationsEnabled;
    private final boolean darkModeEnabled;

    public AppSettings(int dailyLimitHours, boolean notificationsEnabled) {
        this(dailyLimitHours, notificationsEnabled, true);
    }

    public AppSettings(int dailyLimitHours, boolean notificationsEnabled, boolean darkModeEnabled) {
        this.dailyLimitHours = dailyLimitHours;
        this.notificationsEnabled = notificationsEnabled;
        this.darkModeEnabled = darkModeEnabled;
    }

    public int getDailyLimitHours() {
        return dailyLimitHours;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }
}
