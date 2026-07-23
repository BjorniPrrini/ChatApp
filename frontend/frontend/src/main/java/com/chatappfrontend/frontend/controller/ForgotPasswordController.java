package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.service.AuthService;
import com.chatappfrontend.frontend.util.SceneManager;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

public class ForgotPasswordController {
    @FXML
    public TextField emailField;
    @FXML
    public TextField codeField;
    @FXML
    public PasswordField newPasswordField;
    @FXML
    public PasswordField confirmPasswordField;
    @FXML
    public Label errorLabel;
    @FXML
    public Button sendCodeButton;
    @FXML
    public ProgressIndicator loadingSpinner;

    private boolean codeSent = false;
    private boolean codeVerified = false;
    private String userEmail;

    @FXML
    public void handleSendCode(){
        if(codeVerified){
            handleConfirm();

            return;
        }

        if(codeSent){
            handleVerifyCode();

            return;
        }

        String email = emailField.getText().trim();

        userEmail = email;

        if(email.isEmpty()){
            showError("Empty fields");

            return;
        }

        if(!email.contains("@")){
            showError("Not a valid email");

            return;
        }

        sendCodeButton.setDisable(true);
        loadingSpinner.setManaged(true);
        loadingSpinner.setVisible(true);
        errorLabel.setVisible(false);

        try {
            AuthService service = new AuthService();

            service.forgotPassword(email);

            codeSent = true;

            emailField.setVisible(false);
            emailField.setManaged(false);

            codeField.setManaged(true);
            codeField.setVisible(true);

            sendCodeButton.setText("Reset password");
        } catch (Exception e) {
            showError("Can't send code");
        } finally {
            loadingSpinner.setManaged(false);
            loadingSpinner.setVisible(false);
            sendCodeButton.setDisable(false);
        }
    }

    @FXML
    public void goLoginPage(){
        try {
            SceneManager.switchTo("login-page.fxml");
        } catch (Exception e) {
            showError("Can't load to login page");
        }
    }

    private void handleVerifyCode(){
        String code = codeField.getText();

        if(code.isEmpty()){
            showError("Empty field");

            return;
        }

        codeField.setManaged(false);
        codeField.setVisible(false);
        newPasswordField.setManaged(true);
        newPasswordField.setVisible(true);
        confirmPasswordField.setManaged(true);
        confirmPasswordField.setVisible(true);
        sendCodeButton.setText("Confirm");

        codeVerified = true;
    }

    private void handleConfirm(){
        String code = codeField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if(!newPassword.equals(confirmPassword)){
            showError("Confirm password does not match new password");

            return;
        }

        if(newPassword.isEmpty()){
            showError("Empty fields");

            return;
        }

        if(newPassword.length() < 8){
            showError("Password needs to be 8 characters or more");

            return;
        }

        try {
            AuthService service = new AuthService();

            service.resetPassword(userEmail, code, newPassword, confirmPassword);

            SceneManager.switchTo("login-page.fxml");
        } catch (Exception e) {
            showError("Failed to reset password");
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