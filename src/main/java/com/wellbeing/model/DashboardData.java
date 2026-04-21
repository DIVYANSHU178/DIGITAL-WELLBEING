package com.wellbeing.model;

import java.util.List;

public class DashboardData {
    private final long totalScreenTimeSeconds;
    private final List<AppStats> appBreakdown;
    private final String currentActiveApp;
    private final boolean usingSampleData;

    public DashboardData(long totalScreenTimeSeconds, List<AppStats> appBreakdown, String currentActiveApp, boolean usingSampleData) {
        this.totalScreenTimeSeconds = totalScreenTimeSeconds;
        this.appBreakdown = appBreakdown;
        this.currentActiveApp = currentActiveApp;
        this.usingSampleData = usingSampleData;
    }

    public long getTotalScreenTimeSeconds() {
        return totalScreenTimeSeconds;
    }

    public List<AppStats> getAppBreakdown() {
        return appBreakdown;
    }

    public String getCurrentActiveApp() {
        return currentActiveApp;
    }

    public boolean isUsingSampleData() {
        return usingSampleData;
    }
}
