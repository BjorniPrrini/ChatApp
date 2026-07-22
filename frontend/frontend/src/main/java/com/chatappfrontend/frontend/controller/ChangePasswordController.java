package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.service.UserService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.util.Duration;

public class ChangePasswordController {
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label successLabel;

    @FXML
    public void handleChangePassword(){
        String oldPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if(oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()){
            showError("Empty fields");

            return;
        }

        if(!newPassword.equals(confirmPassword)){
            showError("Confirm password does not mach new password");

            return;
        }

        try {
            UserService userService = new UserService();

            userService.changePassword(oldPassword, newPassword, confirmPassword);

            showSuccess("Password changed successfully");

            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();


        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message){
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        pause.setOnFinished(_ -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });

        pause.play();
    }

    private void showSuccess(String message){
        successLabel.setText(message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        pause.setOnFinished(_ -> {
            successLabel.setVisible(false);
            successLabel.setManaged(false);
        });

        pause.play();
    }
}