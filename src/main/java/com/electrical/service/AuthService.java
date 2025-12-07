package com.electrical.service;

import com.electrical.dao.UserDAO;
import com.electrical.model.Role;
import com.electrical.model.User;
import com.electrical.util.PasswordUtils;
import com.electrical.util.SessionManager;
import com.electrical.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Сервис аутентификации и регистрации пользователей
 */
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO;
    
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Попытка входа в систему
     * @param username имя пользователя
     * @param password пароль
     * @return результат аутентификации
     */
    public AuthResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Введите имя пользователя", null);
        }
        
        if (password == null || password.isEmpty()) {
            return new AuthResult(false, "Введите пароль", null);
        }
        
        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        
        if (userOpt.isEmpty()) {
            logger.warn("Попытка входа с несуществующим пользователем: " + username);
            return new AuthResult(false, "Пользователь не найден", null);
        }
        
        User user = userOpt.get();
        
        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            logger.warn("Неверный пароль для пользователя: " + username);
            return new AuthResult(false, "Неверный пароль", null);
        }
        
        SessionManager.setCurrentUser(user);
        logger.info("Пользователь вошёл в систему: " + username);
        
        return new AuthResult(true, "Вход выполнен успешно", user);
    }
    
    /**
     * Регистрация нового пользователя
     * @param username имя пользователя
     * @param password пароль
     * @param confirmPassword подтверждение пароля
     * @param isAdmin создать администратора
     * @return результат регистрации
     */
    public AuthResult register(String username, String password, String confirmPassword, boolean isAdmin) {
        // Валидация имени пользователя
        ValidationUtils.ValidationResult usernameValidation = ValidationUtils.validateUsername(username);
        if (!usernameValidation.valid()) {
            return new AuthResult(false, usernameValidation.errorMessage(), null);
        }
        
        // Валидация пароля
        ValidationUtils.ValidationResult passwordValidation = ValidationUtils.validatePassword(password);
        if (!passwordValidation.valid()) {
            return new AuthResult(false, passwordValidation.errorMessage(), null);
        }
        
        // Проверка совпадения паролей
        ValidationUtils.ValidationResult matchValidation = ValidationUtils.validatePasswordMatch(password, confirmPassword);
        if (!matchValidation.valid()) {
            return new AuthResult(false, matchValidation.errorMessage(), null);
        }
        
        // Проверка уникальности имени пользователя
        if (userDAO.existsByUsername(username.trim())) {
            return new AuthResult(false, "Пользователь с таким именем уже существует", null);
        }
        
        // Создание пользователя
        String passwordHash = PasswordUtils.hashPassword(password);
        Role role = isAdmin ? Role.ADMIN : Role.USER;
        User newUser = new User(username.trim(), passwordHash, role);
        
        userDAO.save(newUser);
        
        logger.info("Новый пользователь зарегистрирован: " + username + " (" + role + ")");
        
        return new AuthResult(true, "Регистрация успешна", newUser);
    }
    
    /**
     * Выход из системы
     */
    public void logout() {
        if (SessionManager.isLoggedIn()) {
            logger.info("Пользователь вышел из системы: " + SessionManager.getCurrentUser().getUsername());
            SessionManager.logout();
        }
    }
    
    /**
     * Результат аутентификации
     */
    public record AuthResult(boolean success, String message, User user) {
    }
}


