package com.wellbeing.ui;

import com.wellbeing.database.DBManager;
import com.wellbeing.model.AppSettings;
import com.wellbeing.service.SettingsService;
import com.wellbeing.tracker.AppTracker;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private AppTracker tracker;

    @Override
    public void start(Stage primaryStage) throws Exception {
        DBManager.initializeDatabase();

        tracker = new AppTracker();
        tracker.startTracking();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainLayout.fxml"));
        Parent root = loader.load();

        SettingsService settingsService = new SettingsService();
        AppSettings appSettings = settingsService.loadSettings();
        root.getStyleClass().removeAll("theme-light", "theme-dark");
        root.getStyleClass().add(appSettings.isDarkModeEnabled() ? "theme-dark" : "theme-light");

        MainController controller = loader.getController();
        controller.initializeApp(tracker);

        Scene scene = new Scene(root, 1320, 820);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("Desktop Digital Wellbeing Monitor");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(720);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (tracker != null) {
            tracker.stopTracking();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
