package com.chatappfrontend.frontend.util;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class AlertUtils {
    public static void showError(Label label, String message){
        show(label, message);
    }

    public static void showSuccess(Label label, String message){
        show(label, message);
    }

    private static void show(Label label, String message){
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(5));

        delay.setOnFinished(_ -> hide(label));

        delay.play();
    }

    public static void hide(Label label){
        label.setVisible(false);
        label.setManaged(false);
    }
}