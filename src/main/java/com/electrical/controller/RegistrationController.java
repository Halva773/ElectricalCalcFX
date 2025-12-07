package com.electrical.controller;

import com.electrical.MainApp;
import com.electrical.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Контроллер окна регистрации
 */
public class RegistrationController implements Initializable {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox adminCheckBox;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    
    private final AuthService authService = new AuthService();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        boolean isAdmin = adminCheckBox.isSelected();
        
        hideMessages();
        
        AuthService.AuthResult result = authService.register(username, password, confirmPassword, isAdmin);
        
        if (result.success()) {
            showSuccess("Регистрация успешна! Выполняется вход.");
            clearFields();
            // Автоматически открываем форму входа
            MainApp.loadLoginView();
        } else {
            showError(result.message());
        }
    }
    
    @FXML
    private void handleBack() {
        MainApp.loadLoginView();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }
    
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
    
    private void hideMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
    
    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        adminCheckBox.setSelected(false);
    }
}


