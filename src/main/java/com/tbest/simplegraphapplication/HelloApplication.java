package com.tbest.simplegraphapplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/fxml/hello-view.fxml"));
        //Square window
        double size = Math.min(Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());
        Scene scene = new Scene(fxmlLoader.load(), size, size);

        stage.setTitle("Lichess Rating Graph");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.show();
    }
}
