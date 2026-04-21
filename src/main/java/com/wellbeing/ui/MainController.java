package com.wellbeing.ui;

import com.wellbeing.service.AnalyticsService;
import com.wellbeing.service.SettingsService;
import com.wellbeing.tracker.AppTracker;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class MainController {

    private enum Screen {
        DASHBOARD("/fxml/dashboard.fxml"),
        APP_DETAILS("/fxml/app_details.fxml"),
        WEEKLY_REPORT("/fxml/weekly_report.fxml"),
        SETTINGS("/fxml/settings.fxml");

        private final String fxmlPath;

        Screen(String fxmlPath) {
            this.fxmlPath = fxmlPath;
        }
    }

    @FXML private StackPane contentContainer;
    @FXML private ToggleButton dashboardButton;
    @FXML private ToggleButton appDetailsButton;
    @FXML private ToggleButton weeklyReportButton;
    @FXML private ToggleButton settingsButton;

    private final ToggleGroup navigationGroup = new ToggleGroup();
    private final Map<Screen, Parent> screenViews = new EnumMap<>(Screen.class);
    private final Map<Screen, ScreenController> screenControllers = new EnumMap<>(Screen.class);
    private Timeline refreshTimeline;
    private Screen activeScreen;
    private AnalyticsService analyticsService;
    private SettingsService settingsService;

    @FXML
    private void initialize() {
        dashboardButton.setToggleGroup(navigationGroup);
        appDetailsButton.setToggleGroup(navigationGroup);
        weeklyReportButton.setToggleGroup(navigationGroup);
        settingsButton.setToggleGroup(navigationGroup);
    }

    public void initializeApp(AppTracker tracker) {
        analyticsService = new AnalyticsService(tracker);
        settingsService = new SettingsService();

        loadScreen(Screen.DASHBOARD);
        loadScreen(Screen.APP_DETAILS);
        loadScreen(Screen.WEEKLY_REPORT);
        loadScreen(Screen.SETTINGS);

        dashboardButton.setSelected(true);
        showScreen(Screen.DASHBOARD);
        startRefreshLoop();
    }

    @FXML
    private void showDashboard() {
        showScreen(Screen.DASHBOARD);
    }

    @FXML
    private void showAppDetails() {
        showScreen(Screen.APP_DETAILS);
    }

    @FXML
    private void showWeeklyReport() {
        showScreen(Screen.WEEKLY_REPORT);
    }

    @FXML
    private void showSettings() {
        showScreen(Screen.SETTINGS);
    }

    private void showScreen(Screen screen) {
        Parent nextView = screenViews.get(screen);
        if (nextView == null) {
            return;
        }

        contentContainer.getChildren().setAll(nextView);
        nextView.setOpacity(0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(260), nextView);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();

        activeScreen = screen;
        refreshActiveView();
    }

    private void loadScreen(Screen screen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screen.fxmlPath));
            Parent view = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ScreenController screenController) {
                screenController.initializeData(analyticsService, settingsService);
                screenViews.put(screen, view);
                screenControllers.put(screen, screenController);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load screen " + screen.fxmlPath, e);
        }
    }

    private void startRefreshLoop() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshActiveView()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void refreshActiveView() {
        if (activeScreen == null) {
            return;
        }
        ScreenController controller = screenControllers.get(activeScreen);
        if (controller != null) {
            controller.refreshView();
        }
    }
}
