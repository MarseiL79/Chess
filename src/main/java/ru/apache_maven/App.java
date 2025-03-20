package ru.apache_maven;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.apache_maven.pieces.CoordinatesShift;
import ru.apache_maven.pieces.Knight;
import ru.apache_maven.pieces.Pawn;
import ru.apache_maven.pieces.Piece;

//import java.util.
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;


public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        //Через FXML файл
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/maket.fxml"));
        Parent panel = loader.load();

        Board board = new Board();
        GameLogic gameLogic = new GameLogic();
        board.setupDefaultPiecePositions(); // Устанавливаем начальные позиции фигур

        ChessController controller = loader.getController();
        controller.setBoard(board); // Передаём board в контроллер
        controller.setGameLogic(gameLogic);

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
