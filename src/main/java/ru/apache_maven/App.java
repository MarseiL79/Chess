package ru.apache_maven;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;


public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        //Через FXML файл
        Parent panel = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/maket.fxml")));

        Scene scene = new Scene(panel, 400, 400);

        primaryStage.setTitle("JavaFX TEST");
        System.out.println("HELLO");
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    public static void main(String[] args) {
        Application.launch();
    }


}
