package ru.apache_maven;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import ru.apache_maven.pieces.Piece;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ChessController implements Initializable {
    @FXML
    private GridPane chessBoardGrid;
    private Board board;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupChessBoard();
    }

    public void setBoard(Board board) {
        this.board = board;
        if (board == null) {
            System.out.println("Ошибка: board == null!");
            return;
        }

        System.out.println("Board установлен, начинаем расстановку фигур!");
        renderPieces(); // Теперь вызываем метод после установки board
    }

    private void setupChessBoard() {

        int boardSize = 8;
        int tileSize = 80;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Pane cell = new Pane();
                //cell.setMaxSize(80, 80);
                cell.setPrefSize(tileSize, tileSize);
                if ((row + col) % 2 == 0) {
                    cell.setStyle("-fx-background-color: beige;");
                } else {
                    cell.setStyle("-fx-background-color: saddlebrown;");
                }

                chessBoardGrid.add(cell, col, row);
            }
        }
    }

    private void renderPieces() {
        if (board == null) {
            System.out.println("Ошибка: Board не установлен, нельзя отрисовать фигуры!");
            return;
        }

        for (Map.Entry<Coordinates, Piece> entry : board.getMatrix().entrySet()) {
            Coordinates coord = entry.getKey();
            Piece piece = entry.getValue();

            InputStream iconStream = getClass().getResourceAsStream("/images/white_pawn.png");
            ImageView pieceImage = new ImageView(new Image(iconStream));
            pieceImage.setFitWidth(80);
            pieceImage.setFitHeight(80);

            chessBoardGrid.add(pieceImage, coord.file.ordinal(), coord.rank - 1);
        }
    }



}
