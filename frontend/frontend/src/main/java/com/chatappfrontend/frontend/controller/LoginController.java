package com.chatappfrontend.frontend.controller;

import com.chatappfrontend.frontend.service.AuthService;
import com.chatappfrontend.frontend.model.AuthResponseDTO;
import com.chatappfrontend.frontend.util.EmailHistoryManager;
import com.chatappfrontend.frontend.util.SceneManager;
import com.chatappfrontend.frontend.util.SessionManager;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

import java.util.List;

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
    private TextField passwordVisibleField;
    @FXML
    private Hyperlink togglePasswordLink;

    private boolean passwordVisible = false;

    @FXML
    public void initialize(){
        ContextMenu emailHistoryMenu = new ContextMenu();

        emailField.setOnMouseClicked(_ -> {
            showEmailHistoryMenu(emailHistoryMenu);
        });

        emailField.setOnAction(_ -> passwordField.requestFocus());

        passwordField.setOnAction(_ -> {
            try {
                handleLogin();
            } catch (Exception e) {
                showError("Failed login");
            }
        });

        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        passwordVisibleField.setOnAction(_ -> {
            try {
                handleLogin();
            } catch (Exception e) {
                showError("Login failed");
            }
        });

        loginButton.setDisable(true);

        emailField.textProperty().addListener((_, _, _) -> updateLoginButtonState());
        passwordField.textProperty().addListener((_, _, _) -> updateLoginButtonState());

    }

    @FXML
    public void handleLogin() throws Exception {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if(email.isEmpty() || password.isEmpty()){
            showError("Empty fields");

            return;
        }

        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        loginButton.setDisable(true);
        loginButton.setText("Logging in...");
        errorLabel.setVisible(false);

        try {
            AuthService authService = new AuthService();

            AuthResponseDTO response = authService.login(email, password);

            EmailHistoryManager.addEmail(email);

            SessionManager.getInstance().setToken(response.getToken());
            SessionManager.getInstance().setUserId(response.getId());
            SessionManager.getInstance().setNickname(response.getNickname());
            SessionManager.getInstance().setProfilePicture(response.getProfilePicture());

            SceneManager.switchTo("chat-page.fxml");
        } catch (Exception e) {
            showError("Invalid email or password");
        } finally {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
            loginButton.setText("Login");
            updateLoginButtonState();
        }
    }

    @FXML
    public void handleTogglePassword(){
        passwordVisible = !passwordVisible;

        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);

        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);

        togglePasswordLink.setText(passwordVisible ? "Hide password" : "Show password");
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

    private void showEmailHistoryMenu(ContextMenu menu){
        List<String> emails = EmailHistoryManager.getEmails();

        if(emails.isEmpty()){
            return;
        }

        menu.getItems().clear();

        for(String email : emails){
            Label emailLabel = new Label(email);

            emailLabel.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(emailLabel, Priority.ALWAYS);

            Button removeButton = new Button("x");

            removeButton.getStyleClass().add("email-history-remove");

            removeButton.setAlignment(Pos.CENTER_RIGHT);

            HBox row = new HBox(10, emailLabel, removeButton);

            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(300);

            CustomMenuItem menuItem = new CustomMenuItem(row);

            menuItem.setHideOnClick(false);

            emailLabel.setOnMouseClicked(_ -> {
                emailField.setText(email);

                passwordField.requestFocus();

                menu.hide();
            });

            removeButton.setOnMouseClicked(_ -> {
                EmailHistoryManager.deleteEmail(email);

                showEmailHistoryMenu(menu);
            });

            menu.getItems().add(menuItem);
        }

        menu.show(emailField, Side.BOTTOM, 0, 0);
    }

    private void updateLoginButtonState(){
        boolean fieldsFilled = !emailField.getText().trim().isEmpty() && !passwordField.getText().trim().isEmpty();

        loginButton.setDisable(!fieldsFilled);
    }

    private void showError(String message){
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(5));

        pause.setOnFinished(_ -> errorLabel.setVisible(false));
        pause.play();
    }
}