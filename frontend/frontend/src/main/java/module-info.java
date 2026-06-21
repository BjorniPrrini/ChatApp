module com.chatappfrontend.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;

    opens com.chatappfrontend.frontend to javafx.fxml;
    exports com.chatappfrontend.frontend;
}