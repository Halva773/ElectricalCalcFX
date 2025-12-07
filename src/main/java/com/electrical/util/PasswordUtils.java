package com.electrical.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Утилиты для работы с паролями (BCrypt хэширование)
 */
public class PasswordUtils {
    
    private static final int BCRYPT_ROUNDS = 12;
    
    private PasswordUtils() {
    }
    
    /**
     * Хэширует пароль с использованием BCrypt
     * @param password исходный пароль
     * @return хэш пароля
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    /**
     * Проверяет соответствие пароля хэшу
     * @param password введённый пароль
     * @param hashedPassword сохранённый хэш
     * @return true если пароль верный
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}


