package ru.apache_maven;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import ru.apache_maven.pieces.Piece;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class ChessController implements Initializable {
    @FXML
    private GridPane chessBoardGrid;
    @FXML
    private Rectangle turnColorRectangle;
    @FXML
    private Label statusLabel;
    private Board board;
    private Piece selectedPiece;
    private ImageView selectedPieceImage;
    private Coordinates selectedCoordinates;
    private HashMap<Pane, String> highlightedCells = new HashMap<>();
    private GameLogic gameLogic;


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

    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    private void setupChessBoard() {
        int boardSize = 8;
        int tileSize = 80;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Pane cell = new Pane();
                cell.getStyleClass().add("grid-cell");
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

            InputStream iconStream = getClass().getResourceAsStream(piece.getPathToImage());
            ImageView pieceImage = new ImageView(new Image(iconStream));
            pieceImage.setFitWidth(80);
            pieceImage.setFitHeight(80);

            pieceImage.setOnMouseClicked(event -> {
                clearHighlights();

                if (selectedPieceImage != null) {
                    if(selectedPiece == piece) {
                        selectedPieceImage.setStyle(""); selectedPiece = null;
                        selectedPieceImage = null; selectedCoordinates = null; highlightedCellsToDefault();
                        highlightedCells.clear(); return;
                    }
                    if(selectedPiece.getColor() != piece.getColor() && selectedPiece.getLegalMoveSquares(statusLabel, board).contains(piece.getCoordinates()))
                        { gameLogic.changeTurnColor(turnColorRectangle); moveSelectedPiece(piece.getCoordinates());
                            showAlertOnCheckmate("Поражение", "Мат королю цвета " + gameLogic.getTurnColor(),
                                    board.isCheckmate(statusLabel, gameLogic.getTurnColor(),
                                            board.isKingInCheck(statusLabel, gameLogic.getTurnColor())));
                            return; } //если скушали фигуру, меняем цвет хода .....
                    selectedPieceImage.setStyle(""); // сброс стиля предыдущего выделения
                    highlightedCellsToDefault();
                }
                if( piece.getColor() == gameLogic.getTurnColor()) {
                    selectedPiece = piece;
                    selectedPieceImage = pieceImage;
                    selectedCoordinates = coord;
                    pieceImage.setStyle("-fx-effect: dropshadow(three-pass-box, blue, 10, 0, 0, 0);");
                    highlightMoves(piece.getLegalMoveSquares(statusLabel, board));
                }
                //System.out.println("Цвет выбранной фигуры: " + piece.getColor());
            });

            chessBoardGrid.add(pieceImage, coord.file.ordinal(), 8 - coord.rank);
        }
    }

    private void highlightedCellsToDefault() {
        for (Map.Entry<Pane, String> entry1 : highlightedCells.entrySet()) {
            entry1.getKey().setStyle(entry1.getValue()); //запомнили пред. стиль - сменили выделение
        }                                                //на стиль клетки, который был до этого
        highlightedCells.clear();
    }

    private void highlightMoves(Set<Coordinates> possibleMoves) {
        // Для каждой клетки, в которую возможен ход, получаем Pane и подсвечиваем её
        for (Coordinates move : possibleMoves) {
            // Используем ту же формулу для вычисления номера строки в GridPane
            Pane cell = (Pane) getNodeByRowColumnIndex(8 - move.rank, move.file.ordinal(), chessBoardGrid);
            if (cell != null) {
                // Сохраняем первоначальный стиль, чтобы потом его восстановить
               // cell.getStyleClass().add("grid-cell");
                highlightedCells.put(cell, cell.getStyle());
                //cell.getStyleClass().add("highlighted-cell");
                cell.setStyle("-fx-background-color: rgb(102,255,130);");
                cell.setOnMouseClicked(e -> {
                    clearHighlights();
                    gameLogic.changeTurnColor(turnColorRectangle);
                    if(selectedPiece == null) {return;}
                    moveSelectedPiece(move);
                    showAlertOnCheckmate("Поражение", "Мат королю цвета " + gameLogic.getTurnColor(),
                            board.isCheckmate(statusLabel, gameLogic.getTurnColor(),
                                    board.isKingInCheck(statusLabel, gameLogic.getTurnColor())));
                     //если ходим на возможный ход, меняем цвет хода
                });
            }
        }
    }


    private void clearHighlights() {
        // Восстанавливаем стиль всех подсвеченных клеток и убираем обработчики
        for (Map.Entry<Pane, String> entry : highlightedCells.entrySet()) {
            Pane cell = entry.getKey();
            cell.setStyle(entry.getValue());
            cell.setOnMouseClicked(null);
        }
        highlightedCells.clear();
    }

    private void moveSelectedPiece(Coordinates target) {
        if (selectedPiece == null) return;
        // Обновляем модель: перемещаем фигуру
        board.movePiece(selectedCoordinates, target);
        // Обновляем UI: перерисовываем доску и фигуры
        chessBoardGrid.getChildren().clear();
        setupChessBoard();
        renderPieces();
        // Сбрасываем выбранную фигуру
        if(!board.isKingInCheck(statusLabel, gameLogic.getTurnColor())) { SoundManager.playMoveSound(); }
        selectedPiece = null;
        selectedPieceImage = null;
        selectedCoordinates = null;
        if(board.isStalemate(gameLogic.getTurnColor())) { showAlertOnStalemate("Пат",
                "Патовая ситуация для короля цвета " + gameLogic.getTurnColor()); }
        else if(board.isKingInCheck(statusLabel, gameLogic.getTurnColor())) {
            SoundManager.playCheckSound();
            statusLabel.setText("Шах");
            statusLabel.setLayoutX(93);
        } else {
            statusLabel.setText("Статус игры");
            statusLabel.setLayoutX(57);
        }
    }

    private void showAlertOnCheckmate(String title, String message, boolean isCheckmate) {
        if(!isCheckmate) { return; }
        SoundManager.playCheckmateSound();
        statusLabel.setText("Мат");
        statusLabel.setLayoutX(93);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlertOnStalemate(String title, String message) {
        SoundManager.playCheckmateSound();
        statusLabel.setText("Пат");
        statusLabel.setLayoutX(93);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Метод для поиска узла по координатам в GridPane:
    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {
                if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                    return node;
                }
            }
        }
        return null;
    }
}
