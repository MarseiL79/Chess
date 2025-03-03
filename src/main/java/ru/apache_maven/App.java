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

        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    public static void main(String[] args) {
        Board board = new Board();
        board.setupDefaultPiecePositions();

        BoardConsoleRenderer renderer = new BoardConsoleRenderer();
        renderer.render(board);

        Application.launch();
    }


}
