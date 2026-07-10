package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.service.AuthService;
import com.chatappfrontend.frontend.model.AuthResponseDTO;

import com.chatappfrontend.frontend.util.SceneManager;
import com.chatappfrontend.frontend.util.SessionManager;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;
    @FXML
    private ProgressIndicator loadingSpinner;

    @FXML
    public void handleLogin() throws Exception {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if(email.isEmpty() || password.isEmpty()){
            showError("Empty fields");

            return;
        }

        loadingSpinner.setVisible(true);
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        try {
            AuthService authService = new AuthService();

            AuthResponseDTO response = authService.login(email, password);

            SessionManager.getInstance().setToken(response.getToken());
            SessionManager.getInstance().setUserId(response.getId());
            SessionManager.getInstance().setNickname(response.getNickname());
            SessionManager.getInstance().setProfilePicture(response.getProfilePicture());

            SceneManager.switchTo("chat.fxml");
        } catch (Exception e) {
            showError("Invalid email or password");
        } finally {
            loadingSpinner.setVisible(false);
            loginButton.setDisable(false);
        }
    }

    @FXML
    public void handleRegister(){
        try {
            SceneManager.switchTo("registration-page.fxml");
        } catch (Exception e) {
            showError("Can't load registration page");
        }
    }

    @FXML
    public void handleForgotPassword(){
        try {
            SceneManager.switchTo("forgot-password.fxml");
        } catch (Exception e) {
            showError("Can't load page");
        }
    }

    private void showError(String message){
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(5));

        pause.setOnFinished(_ -> errorLabel.setVisible(false));
        pause.play();
    }
}