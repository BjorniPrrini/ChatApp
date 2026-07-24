package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.model.UserResponseDTO;
import com.chatappfrontend.frontend.service.UserService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

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
        } catch (Exception e) {
            showError("Could not update profile");
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

    private void loadUsersInformation(){
        try {
            UserService userService = new UserService();

            UserResponseDTO userResponseDTO = userService.getUserInformation();

            nameField.setText(userResponseDTO.getName());
            surnameField.setText(userResponseDTO.getSurname());
            nicknameField.setText(userResponseDTO.getNickname());
            phoneNumberField.setText(userResponseDTO.getPhoneNumber());
            emailField.setText(userResponseDTO.getEmail());
        } catch (Exception e) {
            showError("Could not get users data");
        }
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