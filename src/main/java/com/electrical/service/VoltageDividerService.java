package com.electrical.service;

import com.electrical.dao.CalculationHistoryDAO;
import com.electrical.model.CalculationHistory;
import com.electrical.model.CalculationType;
import com.electrical.model.DividerResult;
import com.electrical.model.ResistorSeries;
import com.electrical.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Сервис расчёта делителя напряжения
 * Vout = Vin * R2 / (R1 + R2)
 */
public class VoltageDividerService {
    
    private static final Logger logger = LoggerFactory.getLogger(VoltageDividerService.class);
    private final CalculationHistoryDAO historyDAO;
    
    public VoltageDividerService() {
        this.historyDAO = new CalculationHistoryDAO();
    }
    
    /**
     * Подобрать комбинации резисторов для делителя напряжения
     * @param vIn входное напряжение
     * @param vOutRequired требуемое выходное напряжение
     * @param tolerancePercent допустимое отклонение в процентах
     * @param series ряд резисторов
     * @param minResistance минимальное сопротивление
     * @param maxResistance максимальное сопротивление
     * @param maxResults максимальное количество результатов
     * @return список подходящих комбинаций
     */
    public List<DividerResult> findDividerCombinations(
            double vIn,
            double vOutRequired,
            double tolerancePercent,
            ResistorSeries series,
            double minResistance,
            double maxResistance,
            int maxResults) {
        
        if (vOutRequired >= vIn) {
            throw new IllegalArgumentException("Выходное напряжение должно быть меньше входного");
        }
        
        if (vOutRequired <= 0) {
            throw new IllegalArgumentException("Выходное напряжение должно быть положительным");
        }
        
        double[] resistors = series.getValuesInRange(minResistance, maxResistance);
        List<DividerResult> results = new ArrayList<>();
        
        // Требуемое соотношение: Vout/Vin = R2/(R1+R2)
        double targetRatio = vOutRequired / vIn;
        
        logger.info("Поиск делителей: Vin={}, Vout={}, tolerance={}%, ряд={}",
                vIn, vOutRequired, tolerancePercent, series);
        
        // 1. Два резистора (классический делитель)
        findTwoResistorCombinations(resistors, vIn, vOutRequired, tolerancePercent, targetRatio, results);
        
        // 2. Три резистора
        findThreeResistorCombinations(resistors, vIn, vOutRequired, tolerancePercent, targetRatio, results);
        
        // 3. Четыре резистора
        findFourResistorCombinations(resistors, vIn, vOutRequired, tolerancePercent, targetRatio, results);
        
        // Сортировка и ограничение результатов
        Collections.sort(results);
        
        if (results.size() > maxResults) {
            results = new ArrayList<>(results.subList(0, maxResults));
        }
        
        logger.info("Найдено {} комбинаций", results.size());
        
        return results;
    }
    
    private void findTwoResistorCombinations(double[] resistors, double vIn, double vOutRequired,
                                              double tolerancePercent, double targetRatio,
                                              List<DividerResult> results) {
        for (double r1 : resistors) {
            for (double r2 : resistors) {
                double vOut = calculateVout(vIn, r1, r2);
                double error = Math.abs((vOut - vOutRequired) / vOutRequired) * 100;
                
                if (error <= tolerancePercent) {
                    results.add(new DividerResult(
                            List.of(r1), List.of(r2),
                            false, false,
                            vIn, vOut, vOutRequired
                    ));
                }
            }
        }
    }
    
    private void findThreeResistorCombinations(double[] resistors, double vIn, double vOutRequired,
                                                double tolerancePercent, double targetRatio,
                                                List<DividerResult> results) {
        // Верхнее плечо: два резистора последовательно или параллельно
        for (double r1a : resistors) {
            for (double r1b : resistors) {
                for (double r2 : resistors) {
                    // Последовательное соединение верхнего плеча
                    double r1Serial = r1a + r1b;
                    double vOutSerial = calculateVout(vIn, r1Serial, r2);
                    double errorSerial = Math.abs((vOutSerial - vOutRequired) / vOutRequired) * 100;
                    
                    if (errorSerial <= tolerancePercent) {
                        results.add(new DividerResult(
                                List.of(r1a, r1b), List.of(r2),
                                false, false,
                                vIn, vOutSerial, vOutRequired
                        ));
                    }
                    
                    // Параллельное соединение верхнего плеча
                    double r1Parallel = (r1a * r1b) / (r1a + r1b);
                    double vOutParallel = calculateVout(vIn, r1Parallel, r2);
                    double errorParallel = Math.abs((vOutParallel - vOutRequired) / vOutRequired) * 100;
                    
                    if (errorParallel <= tolerancePercent) {
                        results.add(new DividerResult(
                                List.of(r1a, r1b), List.of(r2),
                                true, false,
                                vIn, vOutParallel, vOutRequired
                        ));
                    }
                }
            }
        }
        
        // Нижнее плечо: два резистора последовательно или параллельно
        for (double r1 : resistors) {
            for (double r2a : resistors) {
                for (double r2b : resistors) {
                    // Последовательное соединение нижнего плеча
                    double r2Serial = r2a + r2b;
                    double vOutSerial = calculateVout(vIn, r1, r2Serial);
                    double errorSerial = Math.abs((vOutSerial - vOutRequired) / vOutRequired) * 100;
                    
                    if (errorSerial <= tolerancePercent) {
                        results.add(new DividerResult(
                                List.of(r1), List.of(r2a, r2b),
                                false, false,
                                vIn, vOutSerial, vOutRequired
                        ));
                    }
                    
                    // Параллельное соединение нижнего плеча
                    double r2Parallel = (r2a * r2b) / (r2a + r2b);
                    double vOutParallel = calculateVout(vIn, r1, r2Parallel);
                    double errorParallel = Math.abs((vOutParallel - vOutRequired) / vOutRequired) * 100;
                    
                    if (errorParallel <= tolerancePercent) {
                        results.add(new DividerResult(
                                List.of(r1), List.of(r2a, r2b),
                                false, true,
                                vIn, vOutParallel, vOutRequired
                        ));
                    }
                }
            }
        }
    }
    
    private void findFourResistorCombinations(double[] resistors, double vIn, double vOutRequired,
                                               double tolerancePercent, double targetRatio,
                                               List<DividerResult> results) {
        // Оба плеча по два резистора
        // Ограничиваем поиск для оптимизации производительности
        int step = resistors.length > 50 ? 2 : 1;
        
        for (int i1 = 0; i1 < resistors.length; i1 += step) {
            double r1a = resistors[i1];
            for (int i2 = i1; i2 < resistors.length; i2 += step) {
                double r1b = resistors[i2];
                for (int i3 = 0; i3 < resistors.length; i3 += step) {
                    double r2a = resistors[i3];
                    for (int i4 = i3; i4 < resistors.length; i4 += step) {
                        double r2b = resistors[i4];
                        
                        // Комбинации: SS, SP, PS, PP (Serial/Parallel для верхнего/нижнего)
                        checkFourResistorCombination(r1a, r1b, r2a, r2b, false, false, 
                                vIn, vOutRequired, tolerancePercent, results);
                        checkFourResistorCombination(r1a, r1b, r2a, r2b, false, true, 
                                vIn, vOutRequired, tolerancePercent, results);
                        checkFourResistorCombination(r1a, r1b, r2a, r2b, true, false, 
                                vIn, vOutRequired, tolerancePercent, results);
                        checkFourResistorCombination(r1a, r1b, r2a, r2b, true, true, 
                                vIn, vOutRequired, tolerancePercent, results);
                    }
                }
            }
        }
    }
    
    private void checkFourResistorCombination(double r1a, double r1b, double r2a, double r2b,
                                               boolean upperParallel, boolean lowerParallel,
                                               double vIn, double vOutRequired, double tolerancePercent,
                                               List<DividerResult> results) {
        double r1 = upperParallel ? (r1a * r1b) / (r1a + r1b) : r1a + r1b;
        double r2 = lowerParallel ? (r2a * r2b) / (r2a + r2b) : r2a + r2b;
        
        double vOut = calculateVout(vIn, r1, r2);
        double error = Math.abs((vOut - vOutRequired) / vOutRequired) * 100;
        
        if (error <= tolerancePercent) {
            results.add(new DividerResult(
                    List.of(r1a, r1b), List.of(r2a, r2b),
                    upperParallel, lowerParallel,
                    vIn, vOut, vOutRequired
            ));
        }
    }
    
    /**
     * Расчёт выходного напряжения делителя
     * @param vIn входное напряжение
     * @param r1 верхнее сопротивление (к Vin)
     * @param r2 нижнее сопротивление (к GND)
     * @return выходное напряжение
     */
    public double calculateVout(double vIn, double r1, double r2) {
        return vIn * r2 / (r1 + r2);
    }
    
    /**
     * Сохранить результат в историю
     */
    public void saveResultToHistory(DividerResult result) {
        if (SessionManager.isLoggedIn()) {
            String inputParams = String.format("Vin=%.2f В, Vout_треб=%.4f В", 
                    result.getVIn(), result.getVOutRequired());
            String resultStr = String.format("Vout=%.4f В (%.3f%%), R_верх=%s, R_низ=%s",
                    result.getVOutActual(), result.getErrorPercent(),
                    result.getUpperResistorsString(), result.getLowerResistorsString());
            
            CalculationHistory history = new CalculationHistory(
                    SessionManager.getCurrentUser().getId(),
                    CalculationType.VOLTAGE_DIVIDER,
                    inputParams,
                    resultStr
            );
            historyDAO.save(history);
            
            logger.info("Результат делителя сохранён в историю");
        }
    }
}


