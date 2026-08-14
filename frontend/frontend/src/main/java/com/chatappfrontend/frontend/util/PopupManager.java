package com.chatappfrontend.frontend.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.BiConsumer;

public class PopupManager {
    public static <T> void openPopup(String title, String fxmlPath, BiConsumer<T,Stage> setup) throws IOException {
        FXMLLoader loader = new FXMLLoader(PopupManager.class.getResource("/com/chatappfrontend/frontend/views/" + fxmlPath));

        Stage stage = new Stage();

        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(loader.load()));

        setup.accept(loader.getController(), stage);

        stage.showAndWait();
    }
}