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
 * Главный класс JavaFX-приложения {@code Electrical Calculator FX}.
 *
 * <p>Отвечает за:
 * <ul>
 *   <li>запуск приложения и инициализацию главного окна ({@link Stage});</li>
 *   <li>инициализацию базы данных (SQLite) через {@link DatabaseManager};</li>
 *   <li>загрузку и переключение экранов (FXML) с применением общего CSS-стиля;</li>
 *   <li>корректное завершение работы (закрытие соединения с БД).</li>
 * </ul>
 *
 * <p>Навигация по интерфейсу реализована через набор статических методов
 * {@code load*View()}, которые загружают соответствующие FXML-экраны и
 * устанавливают их в {@code primaryStage}.</p>
 *
 * @author —
 * @since 1.0
 */
public class MainApp extends Application {

    /** Логгер приложения. Используется для вывода сообщений о запуске, ошибках и завершении. */
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    /**
     * Основная сцена приложения (главное окно).
     * <p>Используется для установки текущего {@link Scene} при смене экранов.</p>
     */
    private static Stage primaryStage;

    /**
     * Точка входа JavaFX при запуске приложения.
     *
     * <p>Метод выполняет:
     * <ol>
     *   <li>сохранение ссылки на главное окно ({@code primaryStage});</li>
     *   <li>инициализацию базы данных через {@link DatabaseManager#initializeDatabase()};</li>
     *   <li>загрузку стартового экрана авторизации;</li>
     *   <li>настройку параметров окна (заголовок, минимальные размеры) и отображение.</li>
     * </ol>
     *
     * @param stage главное окно приложения, создаваемое JavaFX
     */
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

    /**
     * Загружает экран входа в систему.
     * <p>Использует FXML: {@code /fxml/LoginView.fxml}.</p>
     */
    public static void loadLoginView() {
        loadView("/fxml/LoginView.fxml", "Вход в систему", 450, 400);
    }

    /**
     * Загружает экран регистрации пользователя.
     * <p>Использует FXML: {@code /fxml/RegistrationView.fxml}.</p>
     */
    public static void loadRegistrationView() {
        loadView("/fxml/RegistrationView.fxml", "Регистрация", 450, 500);
    }

    /**
     * Загружает главное меню приложения.
     *
     * <p>Заголовок окна включает имя текущего пользователя, получаемое из
     * {@link SessionManager#getCurrentUser()}.</p>
     *
     * @throws NullPointerException если текущий пользователь не установлен в {@link SessionManager}
     */
    public static void loadMainMenuView() {
        loadView("/fxml/MainMenuView.fxml",
                "Главное меню - " + SessionManager.getCurrentUser().getUsername(),
                900, 700);
    }

    /**
     * Загружает экран калькулятора закона Ома.
     * <p>Использует FXML: {@code /fxml/OhmCalculatorView.fxml}.</p>
     */
    public static void loadOhmCalculatorView() {
        loadView("/fxml/OhmCalculatorView.fxml", "Калькулятор закона Ома", 800, 600);
    }

    /**
     * Загружает экран калькулятора делителя напряжения.
     * <p>Использует FXML: {@code /fxml/VoltageDividerView.fxml}.</p>
     */
    public static void loadVoltageDividerView() {
        loadView("/fxml/VoltageDividerView.fxml", "Калькулятор делителя напряжения", 1200, 800);
    }

    /**
     * Загружает экран истории операций (истории расчётов).
     * <p>Использует FXML: {@code /fxml/HistoryView.fxml}.</p>
     */
    public static void loadHistoryView() {
        loadView("/fxml/HistoryView.fxml", "История операций", 1000, 700);
    }

    /**
     * Универсальный метод загрузки FXML-экрана и установки его в главное окно приложения.
     *
     * <p>Алгоритм работы:
     * <ol>
     *   <li>загрузка FXML по указанному пути;</li>
     *   <li>создание {@link Scene} заданного размера;</li>
     *   <li>подключение CSS-стилей {@code /css/styles.css};</li>
     *   <li>установка сцены и заголовка окна, центрирование.</li>
     * </ol>
     *
     * <p>При ошибке чтения FXML (например, файл отсутствует или содержит ошибки разметки)
     * исключение логируется, а текущая сцена остаётся без изменений.</p>
     *
     * @param fxmlPath путь к FXML в ресурсах (пример: {@code /fxml/LoginView.fxml})
     * @param title заголовок окна
     * @param width ширина сцены в пикселях
     * @param height высота сцены в пикселях
     */
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

    /**
     * Возвращает главное окно приложения.
     *
     * @return {@link Stage} главного окна
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Метод вызывается JavaFX при закрытии приложения.
     *
     * <p>Закрывает соединение с базой данных через {@link DatabaseManager#closeConnection()}
     * и записывает событие в лог.</p>
     */
    @Override
    public void stop() {
        DatabaseManager.getInstance().closeConnection();
        logger.info("Приложение завершено");
    }

    /**
     * Точка входа при запуске из командной строки.
     *
     * <p>Делегирует управление JavaFX через {@link #launch(String...)}.</p>
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch(args);
    }
}
