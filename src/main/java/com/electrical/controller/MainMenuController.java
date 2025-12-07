package com.electrical.controller;

import com.electrical.MainApp;
import com.electrical.service.AuthService;
import com.electrical.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Контроллер главного меню
 */
public class MainMenuController implements Initializable {
    
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Button ohmCalculatorButton;
    @FXML private Button voltageDividerButton;
    @FXML private Button historyButton;
    @FXML private Button logoutButton;
    
    private final AuthService authService = new AuthService();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText("Добро пожаловать, " + SessionManager.getCurrentUser().getUsername() + "!");
            roleLabel.setText("Роль: " + SessionManager.getCurrentUser().getRole().getDisplayName());
        }
    }
    
    @FXML
    private void handleOhmCalculator() {
        MainApp.loadOhmCalculatorView();
    }
    
    @FXML
    private void handleVoltageDivider() {
        MainApp.loadVoltageDividerView();
    }
    
    @FXML
    private void handleHistory() {
        MainApp.loadHistoryView();
    }
    
    @FXML
    private void handleLogout() {
        authService.logout();
        MainApp.loadLoginView();
    }
}


