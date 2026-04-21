package com.wellbeing.service;

import com.wellbeing.database.DBManager;
import com.wellbeing.model.AppDetail;
import com.wellbeing.model.AppStats;
import com.wellbeing.model.DailyUsage;
import com.wellbeing.model.DashboardData;
import com.wellbeing.tracker.AppTracker;
import com.wellbeing.util.AppNameUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsService {
    private final AppTracker tracker;

    public AnalyticsService(AppTracker tracker) {
        this.tracker = tracker;
    }

    public DashboardData getDashboardData() {
        List<AppStats> mergedStats = mergeTodayStats(DBManager.getTodayAppBreakdown());
        long totalSeconds = mergedStats.stream().mapToLong(AppStats::getTotalDurationSeconds).sum();

        if (totalSeconds <= 0) {
            List<AppStats> sample = buildSampleApps();
            return new DashboardData(
                    sample.stream().mapToLong(AppStats::getTotalDurationSeconds).sum(),
                    sample,
                    getCurrentAppName(),
                    true
            );
        }

        return new DashboardData(totalSeconds, mergedStats, getCurrentAppName(), false);
    }

    public List<AppDetail> getAppDetails() {
        List<AppStats> stats = mergeTodayStats(DBManager.getTodayAppDetails());
        if (stats.isEmpty()) {
            return buildSampleApps().stream()
                    .map(stat -> new AppDetail(stat.getAppName(), stat.getTotalDurationSeconds(), stat.getLastUsed()))
                    .collect(Collectors.toList());
        }

        return stats.stream()
                .sorted(Comparator.comparingLong(AppStats::getTotalDurationSeconds).reversed())
                .map(stat -> new AppDetail(stat.getAppName(), stat.getTotalDurationSeconds(), stat.getLastUsed()))
                .collect(Collectors.toList());
    }

    public List<DailyUsage> getWeeklyUsage() {
        List<DailyUsage> weeklyUsage = new ArrayList<>(DBManager.getDailyUsageLast7Days());
        overlayLiveSessionOnWeek(weeklyUsage);

        boolean hasUsage = weeklyUsage.stream().anyMatch(day -> day.getDurationSeconds() > 0);
        if (!hasUsage) {
            return buildSampleWeek();
        }
        return weeklyUsage;
    }

    public String getCurrentAppName() {
        if (tracker == null) {
            return "Tracker unavailable";
        }
        return tracker.getCurrentAppName();
    }

    public long getCurrentSessionDurationSeconds() {
        if (tracker == null) {
            return 0;
        }
        return tracker.getCurrentSessionDurationSeconds();
    }

    private List<AppStats> mergeTodayStats(List<AppStats> persistedStats) {
        Map<String, AppStats> merged = new LinkedHashMap<>();
        for (AppStats stat : persistedStats) {
            String normalizedName = AppNameUtils.normalizeAppName(stat.getAppName());
            merged.put(normalizedName, new AppStats(normalizedName, stat.getTotalDurationSeconds(), stat.getLastUsed()));
        }

        long liveSeconds = getCurrentSessionDurationSeconds();
        String liveApp = getCurrentAppName();
        LocalDateTime startedAt = tracker == null ? null : tracker.getCurrentAppStartTime();
        if (liveSeconds > 0 && liveApp != null && !liveApp.isBlank()) {
            AppStats appStats = merged.computeIfAbsent(liveApp, key -> new AppStats(key, 0, startedAt));
            appStats.addDurationSeconds(liveSeconds);
            appStats.updateLastUsed(LocalDateTime.now());
        }

        return merged.values().stream()
                .sorted(Comparator.comparingLong(AppStats::getTotalDurationSeconds).reversed())
                .collect(Collectors.toList());
    }

    private void overlayLiveSessionOnWeek(List<DailyUsage> week) {
        long liveSeconds = getCurrentSessionDurationSeconds();
        if (liveSeconds <= 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        for (DailyUsage usage : week) {
            if (usage.getDate().equals(today)) {
                usage.addDurationSeconds(liveSeconds);
                return;
            }
        }
    }

    private List<AppStats> buildSampleApps() {
        LocalDateTime now = LocalDateTime.now();
        List<AppStats> sample = new ArrayList<>();
        sample.add(new AppStats(AppNameUtils.normalizeAppName("Chrome.exe"), 58 * 60, now.minusMinutes(4)));
        sample.add(new AppStats(AppNameUtils.normalizeAppName("Code.exe"), 44 * 60, now.minusMinutes(12)));
        sample.add(new AppStats(AppNameUtils.normalizeAppName("Explorer.exe"), 29 * 60, now.minusMinutes(34)));
        sample.add(new AppStats(AppNameUtils.normalizeAppName("Teams.exe"), 22 * 60, now.minusHours(1)));
        return sample;
    }

    private List<DailyUsage> buildSampleWeek() {
        List<DailyUsage> sample = new ArrayList<>();
        long[] durations = {7800, 9200, 6100, 10400, 7200, 8400, 9600};
        LocalDate startDate = LocalDate.now().minusDays(6);
        for (int i = 0; i < durations.length; i++) {
            LocalDate date = startDate.plusDays(i);
            sample.add(new DailyUsage(
                    date,
                    date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    durations[i]
            ));
        }
        return sample;
    }
}
