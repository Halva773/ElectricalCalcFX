package com.electrical.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Менеджер базы данных SQLite
 */
public class DatabaseManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:sqlite:electrical_calc.db";
    private static DatabaseManager instance;
    private Connection connection;
    
    private DatabaseManager() {
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                logger.info("Соединение с базой данных установлено");
            }
        } catch (SQLException e) {
            logger.error("Ошибка подключения к базе данных", e);
        }
        return connection;
    }
    
    public void initializeDatabase() {
        try (Statement stmt = getConnection().createStatement()) {
            
            // Таблица пользователей
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'USER',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Таблица истории расчётов
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS calculation_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    calculation_type TEXT NOT NULL,
                    input_parameters TEXT NOT NULL,
                    result TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);
            
            // Создание индексов
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_history_user_id ON calculation_history(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_history_created_at ON calculation_history(created_at)");
            
            logger.info("База данных инициализирована");
            
        } catch (SQLException e) {
            logger.error("Ошибка инициализации базы данных", e);
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Соединение с базой данных закрыто");
            }
        } catch (SQLException e) {
            logger.error("Ошибка закрытия соединения", e);
        }
    }
}


