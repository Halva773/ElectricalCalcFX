package com.electrical.controller;

import com.electrical.MainApp;
import com.electrical.model.CalculationHistory;
import com.electrical.model.CalculationType;
import com.electrical.service.HistoryService;
import com.electrical.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Контроллер окна истории расчётов
 */
public class HistoryController implements Initializable {
    
    @FXML private TableView<CalculationHistory> historyTable;
    @FXML private TableColumn<CalculationHistory, Long> idColumn;
    @FXML private TableColumn<CalculationHistory, String> dateColumn;
    @FXML private TableColumn<CalculationHistory, String> typeColumn;
    @FXML private TableColumn<CalculationHistory, String> inputColumn;
    @FXML private TableColumn<CalculationHistory, String> resultColumn;
    @FXML private TableColumn<CalculationHistory, String> userColumn;
    
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private CheckBox allUsersCheck;
    @FXML private Button refreshButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    
    @FXML private Label statusLabel;
    @FXML private Label totalLabel;
    
    private final HistoryService historyService = new HistoryService();
    private final ObservableList<CalculationHistory> historyData = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Настройка колонок таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate()));
        typeColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCalculationTypeDisplay()));
        inputColumn.setCellValueFactory(new PropertyValueFactory<>("inputParameters"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        historyTable.setItems(historyData);
        
        // Настройка фильтра по типу
        typeFilterCombo.getItems().addAll("Все типы", "Закон Ома", "Делитель напряжения");
        typeFilterCombo.setValue("Все типы");
        typeFilterCombo.setOnAction(event -> loadHistory());
        
        // Показать/скрыть опцию "все пользователи" в зависимости от роли
        if (!SessionManager.isAdmin()) {
            allUsersCheck.setVisible(false);
            allUsersCheck.setManaged(false);
            userColumn.setVisible(false);
        }
        
        allUsersCheck.setOnAction(event -> loadHistory());
        
        // Обработка выбора строки
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteButton.setDisable(newVal == null);
        });
        
        deleteButton.setDisable(true);
        
        // Загрузка истории
        loadHistory();
    }
    
    @FXML
    private void handleRefresh() {
        loadHistory();
    }
    
    @FXML
    private void handleDelete() {
        CalculationHistory selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Удалить запись из истории?");
            alert.setContentText("Это действие нельзя отменить.");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    historyService.deleteHistoryEntry(selected.getId());
                    loadHistory();
                    statusLabel.setText("Запись удалена");
                }
            });
        }
    }
    
    @FXML
    private void handleClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение очистки");
        alert.setHeaderText("Очистить всю историю?");
        alert.setContentText("Это действие удалит все ваши записи и не может быть отменено.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                historyService.clearCurrentUserHistory();
                loadHistory();
                statusLabel.setText("История очищена");
            }
        });
    }
    
    @FXML
    private void handleBack() {
        MainApp.loadMainMenuView();
    }
    
    private void loadHistory() {
        historyData.clear();
        
        String typeFilter = typeFilterCombo.getValue();
        boolean allUsers = allUsersCheck.isSelected() && SessionManager.isAdmin();
        
        List<CalculationHistory> history;
        
        if ("Все типы".equals(typeFilter)) {
            history = allUsers ? historyService.getAllHistory() : historyService.getCurrentUserHistory();
        } else {
            CalculationType type = "Закон Ома".equals(typeFilter) ? 
                    CalculationType.OHM_LAW : CalculationType.VOLTAGE_DIVIDER;
            history = historyService.getHistoryByType(type, allUsers);
        }
        
        historyData.addAll(history);
        totalLabel.setText("Всего записей: " + history.size());
    }
}


