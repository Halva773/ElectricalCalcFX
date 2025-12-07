package com.electrical.controller;

import com.electrical.MainApp;
import com.electrical.service.OhmCalculatorService;
import com.electrical.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Контроллер калькулятора закона Ома
 */
public class OhmCalculatorController implements Initializable {
    
    @FXML private TextField voltageField;
    @FXML private ComboBox<String> voltageUnitCombo;
    @FXML private CheckBox voltageKnownCheck;
    
    @FXML private TextField currentField;
    @FXML private ComboBox<String> currentUnitCombo;
    @FXML private CheckBox currentKnownCheck;
    
    @FXML private TextField resistanceField;
    @FXML private ComboBox<String> resistanceUnitCombo;
    @FXML private CheckBox resistanceKnownCheck;
    
    @FXML private Button calculateButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    
    @FXML private VBox resultBox;
    @FXML private Label resultLabel;
    @FXML private Label formulaLabel;
    @FXML private Label powerLabel;
    @FXML private Label errorLabel;
    
    private final OhmCalculatorService calculatorService = new OhmCalculatorService();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Инициализация единиц измерения напряжения
        voltageUnitCombo.getItems().addAll("В", "мВ", "кВ");
        voltageUnitCombo.setValue("В");
        
        // Инициализация единиц измерения тока
        currentUnitCombo.getItems().addAll("А", "мА", "мкА");
        currentUnitCombo.setValue("А");
        
        // Инициализация единиц измерения сопротивления
        resistanceUnitCombo.getItems().addAll("Ом", "кОм", "МОм");
        resistanceUnitCombo.setValue("Ом");
        
        resultBox.setVisible(false);
        errorLabel.setVisible(false);
        
        // Связывание чекбоксов с полями ввода
        voltageKnownCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            voltageField.setDisable(!newVal);
            voltageUnitCombo.setDisable(!newVal);
            validateCheckboxes();
        });
        
        currentKnownCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            currentField.setDisable(!newVal);
            currentUnitCombo.setDisable(!newVal);
            validateCheckboxes();
        });
        
        resistanceKnownCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            resistanceField.setDisable(!newVal);
            resistanceUnitCombo.setDisable(!newVal);
            validateCheckboxes();
        });
        
        // По умолчанию известны напряжение и сопротивление
        voltageKnownCheck.setSelected(true);
        resistanceKnownCheck.setSelected(true);
        currentKnownCheck.setSelected(false);
        currentField.setDisable(true);
        currentUnitCombo.setDisable(true);
    }
    
    private void validateCheckboxes() {
        int knownCount = 0;
        if (voltageKnownCheck.isSelected()) knownCount++;
        if (currentKnownCheck.isSelected()) knownCount++;
        if (resistanceKnownCheck.isSelected()) knownCount++;
        
        calculateButton.setDisable(knownCount != 2);
        
        if (knownCount != 2) {
            showError("Выберите ровно два известных параметра");
        } else {
            hideError();
        }
    }
    
    @FXML
    private void handleCalculate() {
        hideError();
        resultBox.setVisible(false);
        
        try {
            if (voltageKnownCheck.isSelected() && currentKnownCheck.isSelected()) {
                // Рассчитать сопротивление
                double voltage = parseValue(voltageField.getText(), voltageUnitCombo.getValue(), "voltage");
                double current = parseValue(currentField.getText(), currentUnitCombo.getValue(), "current");
                
                OhmCalculatorService.OhmResult result = calculatorService.calculateResistance(voltage, current);
                showResult(result, voltage, current, result.resistance());
                formulaLabel.setText("R = V / I = " + OhmCalculatorService.formatVoltage(voltage) + 
                        " / " + OhmCalculatorService.formatCurrent(current));
                
            } else if (voltageKnownCheck.isSelected() && resistanceKnownCheck.isSelected()) {
                // Рассчитать ток
                double voltage = parseValue(voltageField.getText(), voltageUnitCombo.getValue(), "voltage");
                double resistance = parseValue(resistanceField.getText(), resistanceUnitCombo.getValue(), "resistance");
                
                OhmCalculatorService.OhmResult result = calculatorService.calculateCurrent(voltage, resistance);
                showResult(result, voltage, result.current(), resistance);
                formulaLabel.setText("I = V / R = " + OhmCalculatorService.formatVoltage(voltage) + 
                        " / " + OhmCalculatorService.formatResistance(resistance));
                
            } else if (currentKnownCheck.isSelected() && resistanceKnownCheck.isSelected()) {
                // Рассчитать напряжение
                double current = parseValue(currentField.getText(), currentUnitCombo.getValue(), "current");
                double resistance = parseValue(resistanceField.getText(), resistanceUnitCombo.getValue(), "resistance");
                
                OhmCalculatorService.OhmResult result = calculatorService.calculateVoltage(current, resistance);
                showResult(result, result.voltage(), current, resistance);
                formulaLabel.setText("V = I × R = " + OhmCalculatorService.formatCurrent(current) + 
                        " × " + OhmCalculatorService.formatResistance(resistance));
            }
            
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }
    
    private void showResult(OhmCalculatorService.OhmResult result, double voltage, double current, double resistance) {
        resultLabel.setText(result.resultString());
        
        double power = calculatorService.calculatePower(voltage, current);
        String powerStr;
        if (power < 0.001) {
            powerStr = String.format("%.3f мВт", power * 1000);
        } else if (power >= 1000) {
            powerStr = String.format("%.3f кВт", power / 1000);
        } else {
            powerStr = String.format("%.3f Вт", power);
        }
        powerLabel.setText("Мощность: P = V × I = " + powerStr);
        
        resultBox.setVisible(true);
    }
    
    private double parseValue(String text, String unit, String type) {
        if (!ValidationUtils.isPositiveNumber(text)) {
            throw new IllegalArgumentException("Введите корректное положительное число для " + 
                    (type.equals("voltage") ? "напряжения" : type.equals("current") ? "тока" : "сопротивления"));
        }
        
        double value = ValidationUtils.parseDouble(text);
        
        // Конвертация в базовые единицы
        return switch (type) {
            case "voltage" -> switch (unit) {
                case "мВ" -> value / 1000;
                case "кВ" -> value * 1000;
                default -> value;
            };
            case "current" -> switch (unit) {
                case "мА" -> value / 1000;
                case "мкА" -> value / 1_000_000;
                default -> value;
            };
            case "resistance" -> switch (unit) {
                case "кОм" -> value * 1000;
                case "МОм" -> value * 1_000_000;
                default -> value;
            };
            default -> value;
        };
    }
    
    @FXML
    private void handleClear() {
        voltageField.clear();
        currentField.clear();
        resistanceField.clear();
        
        voltageUnitCombo.setValue("В");
        currentUnitCombo.setValue("А");
        resistanceUnitCombo.setValue("Ом");
        
        voltageKnownCheck.setSelected(true);
        resistanceKnownCheck.setSelected(true);
        currentKnownCheck.setSelected(false);
        
        resultBox.setVisible(false);
        hideError();
    }
    
    @FXML
    private void handleBack() {
        MainApp.loadMainMenuView();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void hideError() {
        errorLabel.setVisible(false);
    }
}


