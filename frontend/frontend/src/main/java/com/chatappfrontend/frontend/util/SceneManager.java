package com.chatappfrontend.frontend.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;

public class SceneManager {
    private static Stage primaryStage;

    public static void setStage(Stage stage){
        primaryStage = stage;
    }

    public static void switchTo(String fxmlPath) throws Exception {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/chatappfrontend/frontend/views/" + fxmlPath));

        Scene scene = new Scene(loader.load());

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    public static void switchContent(StackPane container, String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/chatappfrontend/frontend/views/" + fxml));

        container.getChildren().setAll(Collections.singleton(loader.load()));
    }
}