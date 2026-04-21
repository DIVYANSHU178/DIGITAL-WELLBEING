package com.wellbeing.ui;

import com.wellbeing.model.AppSettings;
import com.wellbeing.service.AnalyticsService;
import com.wellbeing.service.SettingsService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;

public class SettingsController implements ScreenController {
    @FXML private TextField timeLimitField;
    @FXML private ToggleButton notificationsToggle;
    @FXML private ToggleButton themeToggle;
    @FXML private Label settingsStatusLabel;
    @FXML private Label settingsSummaryLabel;

    private SettingsService settingsService;

    @Override
    public void initializeData(AnalyticsService analyticsService, SettingsService settingsService) {
        this.settingsService = settingsService;
        notificationsToggle.selectedProperty().addListener((obs, oldValue, selected) ->
                notificationsToggle.setText(selected ? "On" : "Off")
        );
        themeToggle.selectedProperty().addListener((obs, oldValue, selected) -> {
            themeToggle.setText(selected ? "Dark" : "Light");
            applyTheme(themeToggle, selected);
        });
        refreshView();
    }

    @Override
    public void refreshView() {
        AppSettings settings = settingsService.loadSettings();
        timeLimitField.setText(String.valueOf(settings.getDailyLimitHours()));
        notificationsToggle.setSelected(settings.isNotificationsEnabled());
        notificationsToggle.setText(settings.isNotificationsEnabled() ? "On" : "Off");
        themeToggle.setSelected(settings.isDarkModeEnabled());
        themeToggle.setText(settings.isDarkModeEnabled() ? "Dark" : "Light");
        applyTheme(themeToggle, settings.isDarkModeEnabled());
        settingsSummaryLabel.setText("Current limit: " + settings.getDailyLimitHours() + " hours per day");
    }

    @FXML
    private void saveSettings() {
        try {
            int hours = Integer.parseInt(timeLimitField.getText().trim());
            if (hours < 1 || hours > 24) {
                throw new NumberFormatException("Out of range");
            }

            settingsService.saveSettings(new AppSettings(hours, notificationsToggle.isSelected(), themeToggle.isSelected()));
            settingsSummaryLabel.setText("Current limit: " + hours + " hours per day");
            showStatus("Settings saved successfully.", "status-success");
        } catch (NumberFormatException exception) {
            showStatus("Enter a daily limit between 1 and 24 hours.", "status-error");
        }
    }

    private void applyTheme(Node node, boolean darkModeEnabled) {
        if (node == null || node.getScene() == null) {
            return;
        }
        var root = node.getScene().getRoot();
        root.getStyleClass().removeAll("theme-light", "theme-dark");
        root.getStyleClass().add(darkModeEnabled ? "theme-dark" : "theme-light");
    }

    private void showStatus(String message, String styleClass) {
        settingsStatusLabel.setText(message);
        settingsStatusLabel.getStyleClass().removeAll("status-success", "status-error");
        settingsStatusLabel.getStyleClass().add(styleClass);

        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(4));
        pauseTransition.setOnFinished(event -> settingsStatusLabel.setText(""));
        pauseTransition.play();
    }
}
