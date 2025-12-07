package com.electrical;

import com.electrical.dao.DatabaseManager;
import com.electrical.util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Главный класс приложения Electrical Calculator FX
 * Калькулятор для расчётов по закону Ома и делителя напряжения
 */
public class MainApp extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        // Инициализация базы данных
        DatabaseManager.getInstance().initializeDatabase();
        
        // Загрузка окна входа
        loadLoginView();
        
        primaryStage.setTitle("Electrical Calculator FX");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
        
        logger.info("Приложение запущено");
    }
    
    public static void loadLoginView() {
        loadView("/fxml/LoginView.fxml", "Вход в систему", 450, 400);
    }
    
    public static void loadRegistrationView() {
        loadView("/fxml/RegistrationView.fxml", "Регистрация", 450, 500);
    }
    
    public static void loadMainMenuView() {
        loadView("/fxml/MainMenuView.fxml", "Главное меню - " + SessionManager.getCurrentUser().getUsername(), 900, 700);
    }
    
    public static void loadOhmCalculatorView() {
        loadView("/fxml/OhmCalculatorView.fxml", "Калькулятор закона Ома", 800, 600);
    }
    
    public static void loadVoltageDividerView() {
        loadView("/fxml/VoltageDividerView.fxml", "Калькулятор делителя напряжения", 1200, 800);
    }
    
    public static void loadHistoryView() {
        loadView("/fxml/HistoryView.fxml", "История операций", 1000, 700);
    }
    
    private static void loadView(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(MainApp.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            logger.error("Ошибка загрузки представления: " + fxmlPath, e);
        }
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    @Override
    public void stop() {
        DatabaseManager.getInstance().closeConnection();
        logger.info("Приложение завершено");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}


