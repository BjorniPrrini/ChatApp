package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.UserResponseDTO;
import com.chatappfrontend.frontend.service.UserService;
import com.chatappfrontend.frontend.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class EditProfileController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField nicknameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField emailField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label successLabel;

    @FXML
    public void initialize(){
        loadUsersInformation();
    }

    @FXML
    public void handleSaveChanges(){
        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();

        try {
            UserService userService = new UserService();

            userService.editUserProfile(name, surname, nickname, phoneNumber);

            AlertUtils.showSuccess(successLabel, "Saved changes");
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't save changes");
        }
    }

    private void loadUsersInformation(){
        try {
            UserService userService = new UserService();

            UserResponseDTO userResponseDTO = userService.getUserInformation();

            nameField.setText(userResponseDTO.getName());
            surnameField.setText(userResponseDTO.getSurname());
            nicknameField.setText(userResponseDTO.getNickname());
            phoneNumberField.setText(userResponseDTO.getPhoneNumber());
            emailField.setText(userResponseDTO.getEmail());
        } catch (Exception _) {
            AlertUtils.showError(errorLabel, "Couldn't load user information");
        }
    }
}