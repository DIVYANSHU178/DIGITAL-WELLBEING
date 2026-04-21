package com.wellbeing.service;

import com.wellbeing.model.AppSettings;

import java.util.prefs.Preferences;

public class SettingsService {
    private static final String LIMIT_HOURS_KEY = "dailyLimitHours";
    private static final String NOTIFICATIONS_KEY = "notificationsEnabled";
    private static final String DARK_MODE_KEY = "darkModeEnabled";

    private final Preferences preferences = Preferences.userNodeForPackage(SettingsService.class);

    public AppSettings loadSettings() {
        int limitHours = preferences.getInt(LIMIT_HOURS_KEY, 3);
        boolean notificationsEnabled = preferences.getBoolean(NOTIFICATIONS_KEY, true);
        boolean darkModeEnabled = preferences.getBoolean(DARK_MODE_KEY, true);
        return new AppSettings(limitHours, notificationsEnabled, darkModeEnabled);
    }

    public void saveSettings(AppSettings settings) {
        preferences.putInt(LIMIT_HOURS_KEY, settings.getDailyLimitHours());
        preferences.putBoolean(NOTIFICATIONS_KEY, settings.isNotificationsEnabled());
        preferences.putBoolean(DARK_MODE_KEY, settings.isDarkModeEnabled());
    }
}
