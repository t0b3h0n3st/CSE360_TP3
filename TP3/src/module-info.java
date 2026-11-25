/**
 * The Foundations Application module.
 */
module FoundationsF26 {

    requires javafx.controls;
    requires java.sql;
    requires javafx.graphics;
    requires java.desktop;
    requires org.junit.jupiter.api;
    requires java.net.http;

    opens applicationMain to javafx.graphics, javafx.fxml;
}