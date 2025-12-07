package com.electrical.util;

/**
 * Утилиты для валидации данных
 */
public class ValidationUtils {
    
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 6;
    
    private ValidationUtils() {
    }
    
    /**
     * Проверяет корректность имени пользователя
     */
    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "Имя пользователя не может быть пустым");
        }
        
        username = username.trim();
        
        if (username.length() < MIN_USERNAME_LENGTH) {
            return new ValidationResult(false, 
                    "Имя пользователя должно содержать минимум " + MIN_USERNAME_LENGTH + " символов");
        }
        
        if (username.length() > MAX_USERNAME_LENGTH) {
            return new ValidationResult(false, 
                    "Имя пользователя не должно превышать " + MAX_USERNAME_LENGTH + " символов");
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return new ValidationResult(false, 
                    "Имя пользователя может содержать только латинские буквы, цифры и символ подчёркивания");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Проверяет корректность пароля
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Пароль не может быть пустым");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return new ValidationResult(false, 
                    "Пароль должен содержать минимум " + MIN_PASSWORD_LENGTH + " символов");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Проверяет совпадение паролей
     */
    public static ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return new ValidationResult(false, "Пароли не совпадают");
        }
        return new ValidationResult(true, null);
    }
    
    /**
     * Проверяет, что число положительное
     */
    public static boolean isPositiveNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        try {
            double value = Double.parseDouble(text.replace(",", "."));
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Проверяет, что число неотрицательное
     */
    public static boolean isNonNegativeNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        try {
            double value = Double.parseDouble(text.replace(",", "."));
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Парсит число из строки
     */
    public static double parseDouble(String text) {
        return Double.parseDouble(text.replace(",", "."));
    }
    
    /**
     * Результат валидации
     */
    public record ValidationResult(boolean valid, String errorMessage) {
    }
}


