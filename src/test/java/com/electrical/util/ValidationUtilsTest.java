package com.electrical.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для {@link ValidationUtils}.
 *
 * <p>Тестируется record {@link ValidationUtils.ValidationResult},
 * используемый как результат валидации.</p>
 */
class ValidationUtilsTest {

    @Test
    void validationResult_shouldStoreValidState() {
        ValidationUtils.ValidationResult result =
                new ValidationUtils.ValidationResult(true, null);

        assertTrue(result.valid(), "valid должен быть true");
        assertNull(result.errorMessage(), "errorMessage должен быть null при успехе");
    }

    @Test
    void validationResult_shouldStoreErrorMessageWhenInvalid() {
        String error = "Некорректное значение";

        ValidationUtils.ValidationResult result =
                new ValidationUtils.ValidationResult(false, error);

        assertFalse(result.valid(), "valid должен быть false");
        assertEquals(error, result.errorMessage(), "Сообщение об ошибке должно совпадать");
    }

    @Test
    void validationResult_equalsAndHashCode_shouldWork() {
        ValidationUtils.ValidationResult r1 =
                new ValidationUtils.ValidationResult(false, "Ошибка");
        ValidationUtils.ValidationResult r2 =
                new ValidationUtils.ValidationResult(false, "Ошибка");

        assertEquals(r1, r2, "record с одинаковыми данными должны быть равны");
        assertEquals(r1.hashCode(), r2.hashCode(), "hashCode должен совпадать");
    }
}
