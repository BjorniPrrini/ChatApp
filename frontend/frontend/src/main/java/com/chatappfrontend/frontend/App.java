package com.chatappfrontend.frontend;

import com.chatappfrontend.frontend.util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.setStage(stage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chatappfrontend/frontend/views/chat-page.fxml"));

        Scene scene = new Scene(loader.load());

        stage.setTitle("BP-Chat");
        stage.setScene(scene);
        stage.show();
    }
}