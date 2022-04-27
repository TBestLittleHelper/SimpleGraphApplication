module com.tbest.simplegraphapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires chariot;
    requires java.desktop;

    opens com.tbest.simplegraphapplication to javafx.fxml;
    exports com.tbest.simplegraphapplication;
}