package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.service.AuthService;
import com.chatappfrontend.frontend.util.AlertUtils;
import com.chatappfrontend.frontend.util.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.*;

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
            AlertUtils.showError(errorLabel, "Email field is empty");

            return;
        }

        if(!email.contains("@")){
            AlertUtils.showError(errorLabel, "Not a valid email");

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
            AlertUtils.showError(errorLabel, "Couldn't send code");
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
            AlertUtils.showError(errorLabel, "Couldn't load login page");
        }
    }

    private void handleVerifyCode(){
        String code = codeField.getText();

        if(code.isEmpty()){
            AlertUtils.showError(errorLabel, "Empty fields");

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
            AlertUtils.showError(errorLabel, "Confirm password does not mach new password");

            return;
        }

        if(newPassword.isEmpty()){
            AlertUtils.showError(errorLabel, "Empty fields");

            return;
        }

        if(newPassword.length() < 8){
            AlertUtils.showError(errorLabel, "Password should be 8 characters");

            return;
        }

        try {
            AuthService service = new AuthService();

            service.resetPassword(userEmail, code, newPassword, confirmPassword);

            SceneManager.switchTo("login-page.fxml");
        } catch (Exception e) {
            AlertUtils.showError(errorLabel, "Failed to reset password");
        }
    }
}