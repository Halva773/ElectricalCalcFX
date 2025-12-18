package com.electrical.util;

import com.electrical.service.OhmCalculatorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для сервиса {@link OhmCalculatorService}.
 *
 * <p>Тестируются основные вычисления по закону Ома,
 * не зависящие от пользовательского интерфейса.</p>
 */
class OhmCalculatorServiceTest {

    /**
     * Проверяет корректность расчёта мощности по формуле:
     * P = U * I
     */
    @Test
    void calculatePower_shouldReturnVoltageTimesCurrent() {
        OhmCalculatorService service = new OhmCalculatorService();

        double voltage = 12.0;
        double current = 2.0;

        double result = service.calculatePower(voltage, current);

        assertEquals(24.0, result, 1e-9,
                "Мощность должна вычисляться как произведение напряжения и тока");
    }

    /**
     * Проверяет форматирование напряжения.
     * Ожидается, что результат содержит единицу измерения.
     */
    @Test
    void formatVoltage_shouldContainVoltUnit() {
        String formatted = OhmCalculatorService.formatVoltage(5.0);

        assertNotNull(formatted);
        assertTrue(formatted.toLowerCase().contains("в"),
                "Отформатированное напряжение должно содержать единицу измерения");
    }

    /**
     * Проверяет форматирование тока.
     * Ожидается корректное представление значения и единицы измерения.
     */
    @Test
    void formatCurrent_shouldContainAmpereUnit() {
        String formatted = OhmCalculatorService.formatCurrent(0.5);

        assertNotNull(formatted);
        assertTrue(formatted.toLowerCase().contains("а"),
                "Отформатированный ток должен содержать единицу измерения");
    }
}
