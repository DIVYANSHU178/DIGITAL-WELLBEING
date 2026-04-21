package com.wellbeing.ui;

import com.wellbeing.model.AppStats;
import com.wellbeing.model.DashboardData;
import com.wellbeing.service.AnalyticsService;
import com.wellbeing.service.SettingsService;
import com.wellbeing.util.TimeUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class DashboardController implements ScreenController {
    @FXML private Label totalTimeLabel;
    @FXML private Label totalTimeSubLabel;
    @FXML private Label activeInsightLabel;
    @FXML private Label liveAppLabel;
    @FXML private Label liveSessionLabel;
    @FXML private PieChart usagePieChart;
    @FXML private VBox topAppsContainer;

    private AnalyticsService analyticsService;

    @Override
    public void initializeData(AnalyticsService analyticsService, SettingsService settingsService) {
        this.analyticsService = analyticsService;
        usagePieChart.setAnimated(false);
        usagePieChart.setLabelsVisible(true);
        refreshView();
    }

    @Override
    public void refreshView() {
        DashboardData dashboardData = analyticsService.getDashboardData();
        List<AppStats> appBreakdown = dashboardData.getAppBreakdown();
        long totalSeconds = dashboardData.getTotalScreenTimeSeconds();

        totalTimeLabel.setText(TimeUtils.formatDurationShort(totalSeconds));
        totalTimeSubLabel.setText(dashboardData.isUsingSampleData() ? "Sample analytics preview" : "Today");
        liveAppLabel.setText(dashboardData.getCurrentActiveApp());
        liveSessionLabel.setText("Live session: " + TimeUtils.formatDuration(analyticsService.getCurrentSessionDurationSeconds()));

        if (appBreakdown.isEmpty()) {
            activeInsightLabel.setText("Open a few apps to start tracking.");
        } else {
            activeInsightLabel.setText(appBreakdown.get(0).getAppName() + " is leading your screen time today.");
        }

        usagePieChart.setData(FXCollections.observableArrayList(
                appBreakdown.stream()
                        .limit(5)
                        .map(app -> {
                            double percentage = totalSeconds == 0 ? 0 : (app.getTotalDurationSeconds() * 100.0 / totalSeconds);
                            return new PieChart.Data(
                                    String.format("%s %.0f%%", app.getAppName(), percentage),
                                    app.getTotalDurationSeconds()
                            );
                        })
                        .toList()
        ));

        renderTopApps(appBreakdown, totalSeconds);
    }

    private void renderTopApps(List<AppStats> apps, long totalSeconds) {
        topAppsContainer.getChildren().clear();
        List<AppStats> topApps = apps.stream().limit(3).toList();

        if (topApps.isEmpty()) {
            Label label = new Label("No usage recorded yet.");
            label.getStyleClass().add("muted-copy");
            topAppsContainer.getChildren().add(label);
            return;
        }

        for (AppStats app : topApps) {
            VBox container = new VBox(10);
            container.getStyleClass().add("top-app-row");

            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);

            Label appName = new Label(app.getAppName());
            appName.getStyleClass().add("top-app-name");

            Label duration = new Label(TimeUtils.formatDuration(app.getTotalDurationSeconds()));
            duration.getStyleClass().add("top-app-duration");

            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().addAll(appName, spacer, duration);

            ProgressBar progressBar = new ProgressBar(totalSeconds == 0 ? 0 : (double) app.getTotalDurationSeconds() / totalSeconds);
            progressBar.getStyleClass().add("usage-progress");
            progressBar.setMaxWidth(Double.MAX_VALUE);

            container.getChildren().addAll(header, progressBar);
            topAppsContainer.getChildren().add(container);
        }
    }
}
