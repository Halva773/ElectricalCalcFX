package com.electrical.controller;

import com.electrical.MainApp;
import com.electrical.model.DividerResult;
import com.electrical.model.ResistorSeries;
import com.electrical.service.VoltageDividerService;
import com.electrical.util.ValidationUtils;
import com.electrical.view.CircuitDrawer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Контроллер калькулятора делителя напряжения
 */
public class VoltageDividerController implements Initializable {
    
    @FXML private TextField vInField;
    @FXML private TextField vOutField;
    @FXML private TextField toleranceField;
    @FXML private ComboBox<ResistorSeries> seriesCombo;
    @FXML private TextField minResistanceField;
    @FXML private TextField maxResistanceField;
    @FXML private ComboBox<String> minResUnitCombo;
    @FXML private ComboBox<String> maxResUnitCombo;
    
    @FXML private Button calculateButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    @FXML private Button saveButton;
    
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    @FXML private Label errorLabel;
    
    @FXML private TableView<DividerResultRow> resultsTable;
    @FXML private TableColumn<DividerResultRow, Integer> indexColumn;
    @FXML private TableColumn<DividerResultRow, String> vOutColumn;
    @FXML private TableColumn<DividerResultRow, String> errorColumn;
    @FXML private TableColumn<DividerResultRow, String> upperColumn;
    @FXML private TableColumn<DividerResultRow, String> lowerColumn;
    @FXML private TableColumn<DividerResultRow, Integer> countColumn;
    @FXML private TableColumn<DividerResultRow, String> typeColumn;
    
    @FXML private Canvas circuitCanvas;
    @FXML private VBox schemaBox;
    @FXML private Label schemaInfoLabel;
    
    private final VoltageDividerService dividerService = new VoltageDividerService();
    private final ObservableList<DividerResultRow> resultRows = FXCollections.observableArrayList();
    private List<DividerResult> currentResults;
    private DividerResult selectedResult;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Инициализация рядов резисторов
        seriesCombo.getItems().addAll(ResistorSeries.values());
        seriesCombo.setValue(ResistorSeries.E24);
        
        // Единицы измерения сопротивления
        minResUnitCombo.getItems().addAll("Ом", "кОм", "МОм");
        minResUnitCombo.setValue("Ом");
        maxResUnitCombo.getItems().addAll("Ом", "кОм", "МОм");
        maxResUnitCombo.setValue("МОм");
        
        // Значения по умолчанию
        vInField.setText("12");
        vOutField.setText("5");
        toleranceField.setText("1");
        minResistanceField.setText("100");
        maxResistanceField.setText("1");
        
        // Настройка таблицы
        indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
        vOutColumn.setCellValueFactory(new PropertyValueFactory<>("vOut"));
        errorColumn.setCellValueFactory(new PropertyValueFactory<>("error"));
        upperColumn.setCellValueFactory(new PropertyValueFactory<>("upperResistors"));
        lowerColumn.setCellValueFactory(new PropertyValueFactory<>("lowerResistors"));
        countColumn.setCellValueFactory(new PropertyValueFactory<>("resistorCount"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("schemaType"));
        
        resultsTable.setItems(resultRows);
        
        // Обработка выбора строки в таблице
        resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && currentResults != null && newVal.getIndex() <= currentResults.size()) {
                selectedResult = currentResults.get(newVal.getIndex() - 1);
                drawCircuit(selectedResult);
                saveButton.setDisable(false);
            }
        });
        
        progressIndicator.setVisible(false);
        errorLabel.setVisible(false);
        saveButton.setDisable(true);
        schemaBox.setVisible(false);
    }
    
    @FXML
    private void handleCalculate() {
        hideError();
        resultRows.clear();
        schemaBox.setVisible(false);
        saveButton.setDisable(true);
        
        try {
            double vIn = ValidationUtils.parseDouble(vInField.getText());
            double vOut = ValidationUtils.parseDouble(vOutField.getText());
            double tolerance = ValidationUtils.parseDouble(toleranceField.getText());
            double minRes = parseResistance(minResistanceField.getText(), minResUnitCombo.getValue());
            double maxRes = parseResistance(maxResistanceField.getText(), maxResUnitCombo.getValue());
            ResistorSeries series = seriesCombo.getValue();
            
            if (vIn <= 0) {
                showError("Входное напряжение должно быть положительным");
                return;
            }
            if (vOut <= 0 || vOut >= vIn) {
                showError("Выходное напряжение должно быть положительным и меньше входного");
                return;
            }
            if (tolerance <= 0) {
                showError("Допуск должен быть положительным");
                return;
            }
            if (minRes >= maxRes) {
                showError("Минимальное сопротивление должно быть меньше максимального");
                return;
            }
            
            // Запуск расчёта в фоновом потоке
            calculateButton.setDisable(true);
            progressIndicator.setVisible(true);
            statusLabel.setText("Поиск комбинаций...");
            
            final double finalVIn = vIn;
            final double finalVOut = vOut;
            final double finalTolerance = tolerance;
            final double finalMinRes = minRes;
            final double finalMaxRes = maxRes;
            
            Task<List<DividerResult>> task = new Task<>() {
                @Override
                protected List<DividerResult> call() {
                    return dividerService.findDividerCombinations(
                            finalVIn, finalVOut, finalTolerance, series, finalMinRes, finalMaxRes, 100);
                }
            };
            
            task.setOnSucceeded(event -> {
                currentResults = task.getValue();
                displayResults(currentResults);
                calculateButton.setDisable(false);
                progressIndicator.setVisible(false);
                statusLabel.setText("Найдено комбинаций: " + currentResults.size());
            });
            
            task.setOnFailed(event -> {
                showError("Ошибка расчёта: " + task.getException().getMessage());
                calculateButton.setDisable(false);
                progressIndicator.setVisible(false);
                statusLabel.setText("");
            });
            
            new Thread(task).start();
            
        } catch (NumberFormatException e) {
            showError("Введите корректные числовые значения");
        }
    }
    
    private void displayResults(List<DividerResult> results) {
        Platform.runLater(() -> {
            resultRows.clear();
            int index = 1;
            for (DividerResult result : results) {
                resultRows.add(new DividerResultRow(index++, result));
            }
            
            if (!results.isEmpty()) {
                resultsTable.getSelectionModel().selectFirst();
            }
        });
    }
    
    private void drawCircuit(DividerResult result) {
        schemaBox.setVisible(true);
        CircuitDrawer drawer = new CircuitDrawer(circuitCanvas);
        drawer.drawVoltageDivider(result);
        
        schemaInfoLabel.setText(String.format(
                "Vout = %.4f В | Погрешность: %.3f%% | Ток: %.3f мА | Мощность: %.3f мВт",
                result.getVOutActual(),
                result.getErrorPercent(),
                result.getCurrent() * 1000,
                result.getPowerDissipation() * 1000
        ));
    }
    
    @FXML
    private void handleSave() {
        if (selectedResult != null) {
            dividerService.saveResultToHistory(selectedResult);
            statusLabel.setText("Результат сохранён в историю");
        }
    }
    
    @FXML
    private void handleClear() {
        vInField.setText("12");
        vOutField.setText("5");
        toleranceField.setText("1");
        minResistanceField.setText("100");
        maxResistanceField.setText("1");
        seriesCombo.setValue(ResistorSeries.E24);
        minResUnitCombo.setValue("Ом");
        maxResUnitCombo.setValue("МОм");
        
        resultRows.clear();
        currentResults = null;
        selectedResult = null;
        schemaBox.setVisible(false);
        saveButton.setDisable(true);
        hideError();
        statusLabel.setText("");
    }
    
    @FXML
    private void handleBack() {
        MainApp.loadMainMenuView();
    }
    
    private double parseResistance(String text, String unit) {
        double value = ValidationUtils.parseDouble(text);
        return switch (unit) {
            case "кОм" -> value * 1000;
            case "МОм" -> value * 1_000_000;
            default -> value;
        };
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void hideError() {
        errorLabel.setVisible(false);
    }
    
    /**
     * Класс-обёртка для отображения результата в таблице
     */
    public static class DividerResultRow {
        private final int index;
        private final String vOut;
        private final String error;
        private final String upperResistors;
        private final String lowerResistors;
        private final int resistorCount;
        private final String schemaType;
        
        public DividerResultRow(int index, DividerResult result) {
            this.index = index;
            this.vOut = String.format("%.4f В", result.getVOutActual());
            this.error = String.format("%.3f%%", result.getErrorPercent());
            this.upperResistors = result.getUpperResistorsString();
            this.lowerResistors = result.getLowerResistorsString();
            this.resistorCount = result.getTotalResistorCount();
            
            String upper = result.getUpperResistors().size() > 1 ? 
                    (result.isUpperParallel() ? "||" : "+") : "1";
            String lower = result.getLowerResistors().size() > 1 ? 
                    (result.isLowerParallel() ? "||" : "+") : "1";
            this.schemaType = upper + "/" + lower;
        }
        
        public int getIndex() { return index; }
        public String getVOut() { return vOut; }
        public String getError() { return error; }
        public String getUpperResistors() { return upperResistors; }
        public String getLowerResistors() { return lowerResistors; }
        public int getResistorCount() { return resistorCount; }
        public String getSchemaType() { return schemaType; }
    }
}


