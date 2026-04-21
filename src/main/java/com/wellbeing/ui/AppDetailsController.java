package com.wellbeing.ui;

import com.wellbeing.model.AppDetail;
import com.wellbeing.service.AnalyticsService;
import com.wellbeing.service.SettingsService;
import com.wellbeing.util.TimeUtils;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AppDetailsController implements ScreenController {
    @FXML private TableView<AppDetail> appDetailsTable;
    @FXML private TableColumn<AppDetail, String> appNameColumn;
    @FXML private TableColumn<AppDetail, Number> timeUsedColumn;
    @FXML private TableColumn<AppDetail, String> lastUsedColumn;

    private AnalyticsService analyticsService;

    @Override
    public void initializeData(AnalyticsService analyticsService, SettingsService settingsService) {
        this.analyticsService = analyticsService;

        appDetailsTable.setPlaceholder(new Label("No app usage yet. Open apps today to populate the list."));
        appNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAppName()));
        timeUsedColumn.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getTimeUsedTodaySeconds()));
        lastUsedColumn.setCellValueFactory(data -> new SimpleStringProperty(TimeUtils.formatTimeAgo(data.getValue().getLastUsed())));

        timeUsedColumn.setComparator((left, right) -> Long.compare(left.longValue(), right.longValue()));
        timeUsedColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : TimeUtils.formatDuration(item.longValue()));
            }
        });

        timeUsedColumn.setSortType(TableColumn.SortType.DESCENDING);
        appDetailsTable.getSortOrder().add(timeUsedColumn);
        refreshView();
    }

    @Override
    public void refreshView() {
        appDetailsTable.setItems(FXCollections.observableArrayList(analyticsService.getAppDetails()));
        appDetailsTable.sort();
    }
}
