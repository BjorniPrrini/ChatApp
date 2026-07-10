package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.AuthResponseDTO;
import com.chatappfrontend.frontend.service.AuthService;
import com.chatappfrontend.frontend.util.SceneManager;
import com.chatappfrontend.frontend.util.SessionManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

public class RegistrationController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField nicknameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button registerButton;
    @FXML
    private ProgressIndicator loadingSpinner;

    @FXML
    public void handleRegister(){
        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();

        if(name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            showError("Empty fields");

            return;
        }

        if(!password.equals(confirmPassword)){
            showError("Confirm password does not mach password");

            return;
        }

        if(password.length() < 8){
            showError("Password length should be 8 characters or more");

            return;
        }

        if(!email.contains("@")){
            showError("Not a valid email");

            return;
        }

        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        registerButton.setDisable(true);
        errorLabel.setVisible(false);

        try {
            AuthService authService = new AuthService();

            AuthResponseDTO response = authService.register(name, surname, email, password, confirmPassword, nickname, phoneNumber);

            SessionManager.getInstance().setToken(response.getToken());
            SessionManager.getInstance().setUserId(response.getId());
            SessionManager.getInstance().setNickname(response.getNickname());
            SessionManager.getInstance().setProfilePicture(response.getProfilePicture());

            SceneManager.switchTo("chat.fxml");
        } catch (Exception e) {
            showError("Registration failed");
        } finally {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            registerButton.setDisable(false);
        }
    }

    @FXML
    public void handleLogin(){
        try {
            SceneManager.switchTo("login-page.fxml");
        } catch (Exception e) {
            showError("Can't load login page");
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