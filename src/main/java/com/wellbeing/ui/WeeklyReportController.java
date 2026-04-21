package com.wellbeing.ui;

import com.wellbeing.model.DailyUsage;
import com.wellbeing.service.AnalyticsService;
import com.wellbeing.service.SettingsService;
import com.wellbeing.util.TimeUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.List;

public class WeeklyReportController implements ScreenController {
    @FXML private BarChart<String, Number> weeklyBarChart;
    @FXML private Label weeklySummaryLabel;

    private AnalyticsService analyticsService;

    @Override
    public void initializeData(AnalyticsService analyticsService, SettingsService settingsService) {
        this.analyticsService = analyticsService;
        weeklyBarChart.setAnimated(false);
        refreshView();
    }

    @Override
    public void refreshView() {
        List<DailyUsage> weeklyUsage = analyticsService.getWeeklyUsage();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (DailyUsage usage : weeklyUsage) {
            double hours = usage.getDurationSeconds() / 3600.0;
            XYChart.Data<String, Number> data = new XYChart.Data<>(usage.getDayLabel(), hours);
            series.getData().add(data);
            data.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {
                    node.setStyle("-fx-bar-fill: linear-gradient(to bottom, #4ea8ff, #1f5ccd);");
                }
            });
        }

        weeklyBarChart.getData().setAll(series);
        long totalSeconds = weeklyUsage.stream().mapToLong(DailyUsage::getDurationSeconds).sum();
        weeklySummaryLabel.setText("Last 7 days total: " + TimeUtils.formatDurationShort(totalSeconds));
    }
}
