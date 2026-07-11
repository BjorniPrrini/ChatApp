module com.chatappfrontend.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires static lombok;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;

    opens com.chatappfrontend.frontend to javafx.fxml;
    opens com.chatappfrontend.frontend.controller to javafx.fxml;
    opens com.chatappfrontend.frontend.model to com.fasterxml.jackson.databind;

    exports com.chatappfrontend.frontend;
    exports com.chatappfrontend.frontend.controller;
}