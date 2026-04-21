package com.wellbeing.ui;

import com.wellbeing.service.AnalyticsService;
import com.wellbeing.service.SettingsService;

public interface ScreenController {
    void initializeData(AnalyticsService analyticsService, SettingsService settingsService);

    void refreshView();
}
