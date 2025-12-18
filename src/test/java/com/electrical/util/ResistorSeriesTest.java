package com.electrical.util;

import com.electrical.model.ResistorSeries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для перечисления {@link ResistorSeries}.
 *
 * <p>Проверяется корректность данных рядов стандартных номиналов
 * резисторов (E6, E12, E24, E96).</p>
 */
class ResistorSeriesTest {

    /**
     * Проверяет, что каждый ряд резисторов имеет имя.
     */
    @Test
    void resistorSeries_shouldHaveNonEmptyName() {
        for (ResistorSeries series : ResistorSeries.values()) {
            assertNotNull(series.getName(),
                    "Имя ряда не должно быть null");
            assertFalse(series.getName().isBlank(),
                    "Имя ряда не должно быть пустым");
        }
    }

    /**
     * Проверяет, что каждый ряд содержит базовые значения номиналов.
     */
    @Test
    void resistorSeries_shouldContainBaseValues() {
        for (ResistorSeries series : ResistorSeries.values()) {
            double[] values = series.getBaseValues();

            assertNotNull(values,
                    "Массив номиналов не должен быть null");
            assertTrue(values.length > 0,
                    "Ряд резисторов должен содержать хотя бы одно значение");
        }
    }

    /**
     * Проверяет, что все номиналы в рядах являются положительными числами.
     */
    @Test
    void resistorSeries_baseValuesShouldBePositive() {
        for (ResistorSeries series : ResistorSeries.values()) {
            for (double value : series.getBaseValues()) {
                assertTrue(value > 0,
                        "Номинал резистора должен быть положительным");
            }
        }
    }
}
