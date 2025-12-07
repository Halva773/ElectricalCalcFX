package com.electrical.service;

import com.electrical.dao.CalculationHistoryDAO;
import com.electrical.model.CalculationHistory;
import com.electrical.model.CalculationType;
import com.electrical.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Сервис расчётов по закону Ома
 * V = I * R
 * I = V / R
 * R = V / I
 */
public class OhmCalculatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(OhmCalculatorService.class);
    private final CalculationHistoryDAO historyDAO;
    
    public OhmCalculatorService() {
        this.historyDAO = new CalculationHistoryDAO();
    }
    
    /**
     * Рассчитать напряжение по току и сопротивлению
     * @param current ток в амперах
     * @param resistance сопротивление в омах
     * @return напряжение в вольтах
     */
    public OhmResult calculateVoltage(double current, double resistance) {
        double voltage = current * resistance;
        
        String inputParams = String.format("I = %s, R = %s", 
                formatCurrent(current), formatResistance(resistance));
        String result = String.format("V = %s", formatVoltage(voltage));
        
        saveToHistory(inputParams, result);
        
        logger.info("Расчёт напряжения: " + result);
        
        return new OhmResult(voltage, 0, 0, "voltage", inputParams, result);
    }
    
    /**
     * Рассчитать ток по напряжению и сопротивлению
     * @param voltage напряжение в вольтах
     * @param resistance сопротивление в омах
     * @return ток в амперах
     */
    public OhmResult calculateCurrent(double voltage, double resistance) {
        if (resistance == 0) {
            throw new IllegalArgumentException("Сопротивление не может быть равно нулю");
        }
        
        double current = voltage / resistance;
        
        String inputParams = String.format("V = %s, R = %s", 
                formatVoltage(voltage), formatResistance(resistance));
        String result = String.format("I = %s", formatCurrent(current));
        
        saveToHistory(inputParams, result);
        
        logger.info("Расчёт тока: " + result);
        
        return new OhmResult(0, current, 0, "current", inputParams, result);
    }
    
    /**
     * Рассчитать сопротивление по напряжению и току
     * @param voltage напряжение в вольтах
     * @param current ток в амперах
     * @return сопротивление в омах
     */
    public OhmResult calculateResistance(double voltage, double current) {
        if (current == 0) {
            throw new IllegalArgumentException("Ток не может быть равен нулю");
        }
        
        double resistance = voltage / current;
        
        String inputParams = String.format("V = %s, I = %s", 
                formatVoltage(voltage), formatCurrent(current));
        String result = String.format("R = %s", formatResistance(resistance));
        
        saveToHistory(inputParams, result);
        
        logger.info("Расчёт сопротивления: " + result);
        
        return new OhmResult(0, 0, resistance, "resistance", inputParams, result);
    }
    
    /**
     * Рассчитать мощность
     */
    public double calculatePower(double voltage, double current) {
        return voltage * current;
    }
    
    private void saveToHistory(String inputParams, String result) {
        if (SessionManager.isLoggedIn()) {
            CalculationHistory history = new CalculationHistory(
                    SessionManager.getCurrentUser().getId(),
                    CalculationType.OHM_LAW,
                    inputParams,
                    result
            );
            historyDAO.save(history);
        }
    }
    
    public static String formatVoltage(double voltage) {
        if (Math.abs(voltage) < 0.001) {
            return String.format("%.3f мкВ", voltage * 1_000_000);
        } else if (Math.abs(voltage) < 1) {
            return String.format("%.3f мВ", voltage * 1000);
        } else if (Math.abs(voltage) >= 1000) {
            return String.format("%.3f кВ", voltage / 1000);
        } else {
            return String.format("%.3f В", voltage);
        }
    }
    
    public static String formatCurrent(double current) {
        if (Math.abs(current) < 0.000001) {
            return String.format("%.3f нА", current * 1_000_000_000);
        } else if (Math.abs(current) < 0.001) {
            return String.format("%.3f мкА", current * 1_000_000);
        } else if (Math.abs(current) < 1) {
            return String.format("%.3f мА", current * 1000);
        } else {
            return String.format("%.3f А", current);
        }
    }
    
    public static String formatResistance(double resistance) {
        if (resistance >= 1_000_000) {
            return String.format("%.3f МОм", resistance / 1_000_000);
        } else if (resistance >= 1000) {
            return String.format("%.3f кОм", resistance / 1000);
        } else if (resistance < 1) {
            return String.format("%.3f мОм", resistance * 1000);
        } else {
            return String.format("%.3f Ом", resistance);
        }
    }
    
    /**
     * Результат расчёта по закону Ома
     */
    public record OhmResult(
            double voltage,
            double current,
            double resistance,
            String calculatedParam,
            String inputParameters,
            String resultString
    ) {
    }
}


