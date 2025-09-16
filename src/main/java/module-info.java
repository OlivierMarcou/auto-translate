module net.arkaine {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    requires com.google.gson;
    exports net.arkaine;
    opens net.arkaine to javafx.fxml, com.google.gson;
}