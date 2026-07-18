package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.AuthResponseDTO;
import com.chatappfrontend.frontend.service.AuthService;
import com.chatappfrontend.frontend.util.EmailHistoryManager;
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
    private TextField passwordVisibleField;
    @FXML
    private TextField confirmPasswordVisibleField;
    @FXML
    private Hyperlink togglePasswordLink;

    private boolean passwordVisible = false;

    @FXML
    public void initialize(){
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        registerButton.setDisable(true);

        nameField.textProperty().addListener((_, _, _) -> updateRegisterButtonState());
        surnameField.textProperty().addListener((_, _, _) -> updateRegisterButtonState());
        emailField.textProperty().addListener((_, _, _) -> updateRegisterButtonState());
        passwordField.textProperty().addListener((_, _, _) -> updateRegisterButtonState());
        confirmPasswordField.textProperty().addListener((_, _, _) -> updateRegisterButtonState());

    }

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
        registerButton.setText("Registering...");
        errorLabel.setVisible(false);

        try {
            AuthService authService = new AuthService();

            AuthResponseDTO response = authService.register(name, surname, email, password, confirmPassword, nickname, phoneNumber);

            EmailHistoryManager.addEmail(email);

            SessionManager.getInstance().setToken(response.getToken());
            SessionManager.getInstance().setUserId(response.getId());
            SessionManager.getInstance().setNickname(response.getNickname());
            SessionManager.getInstance().setProfilePicture(response.getProfilePicture());

            SceneManager.switchTo("chat-page.fxml");
        } catch (Exception e) {
            showError("Registration failed");
        } finally {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            registerButton.setText("Register");
            updateRegisterButtonState();
        }
    }

    @FXML
    public void handleTogglePassword(){
        passwordVisible = !passwordVisible;

        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);

        confirmPasswordField.setVisible(!passwordVisible);
        confirmPasswordField.setManaged(!passwordVisible);
        confirmPasswordVisibleField.setVisible(passwordVisible);
        confirmPasswordVisibleField.setManaged(passwordVisible);

        togglePasswordLink.setText(passwordVisible ? "Hide passwords" : "Show passwords");
    }

    @FXML
    public void handleLogin(){
        try {
            SceneManager.switchTo("login-page.fxml");
        } catch (Exception e) {
            showError("Can't load login page");
        }
    }

    private void updateRegisterButtonState(){
        boolean filled = !nameField.getText().trim().isEmpty() && !surnameField.getText().trim().isEmpty() && !emailField.getText().trim().isEmpty() && !passwordField.getText().trim().isEmpty() && !confirmPasswordField.getText().trim().isEmpty();

        registerButton.setDisable(!filled);
    }

    private void showError(String message){
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(5));

        pause.setOnFinished(_ -> errorLabel.setVisible(false));

        pause.play();
    }
}