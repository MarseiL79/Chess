package ru.apache_maven;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.apache_maven.online.OnlineChessClient;

import java.io.InputStream;


public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/maket.fxml"));
        Parent panel = loader.load();

        Board board = new Board();
        GameLogic gameLogic = new GameLogic();
        board.setupDefaultPiecePositions();

        ChessController controller = loader.getController();
        controller.setBoard(board);
        controller.setGameLogic(gameLogic);

        // Создаём объект клиента, например, для подключения к localhost на порту 12345
        OnlineChessClient onlineClient = new OnlineChessClient("localhost", 12345, controller);
        onlineClient.start();
        controller.setOnlineClient(onlineClient);

        Scene scene = new Scene(panel, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        InputStream iconStream = getClass().getResourceAsStream("/images/pion.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        Application.launch();
    }


}
