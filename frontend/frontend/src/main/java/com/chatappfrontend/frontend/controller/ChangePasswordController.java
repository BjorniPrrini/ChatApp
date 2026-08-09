package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.service.UserService;
import com.chatappfrontend.frontend.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

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
            AlertUtils.showError(errorLabel, "Empty fields");

            return;
        }

        if(!newPassword.equals(confirmPassword)){
            AlertUtils.showError(errorLabel, "Confirm password is not the same as new password");

            return;
        }

        try {
            UserService userService = new UserService();

            userService.changePassword(oldPassword, newPassword, confirmPassword);

            AlertUtils.showSuccess(successLabel, "Password changed");

            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } catch (Exception e) {
            AlertUtils.showError(errorLabel, "Couldn't change password");
        }
    }
}