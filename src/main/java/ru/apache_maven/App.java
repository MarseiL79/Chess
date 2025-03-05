package ru.apache_maven;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.apache_maven.pieces.CoordinatesShift;
import ru.apache_maven.pieces.Piece;

//import java.util.
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;


public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        //Через FXML файл
        Parent panel = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/maket.fxml")));

        Scene scene = new Scene(panel, 400, 400);

        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        InputStream iconStream = getClass().getResourceAsStream("/images/pion.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);

    }

    public static void main(String[] args) {
        Board board = new Board();
        board.setupDefaultPiecePositions();

        BoardConsoleRenderer renderer = new BoardConsoleRenderer();
        renderer.render(board);

        Piece piece = board.getPiece(new Coordinates(File.B, 1));
        Set<Coordinates> availableMoves = piece.getAvailableMoveSquares(board);

        int a = 123;

        Application.launch();
    }


}
