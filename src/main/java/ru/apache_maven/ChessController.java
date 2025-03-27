package ru.apache_maven;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import ru.apache_maven.online.OnlineChessClient;
import ru.apache_maven.pieces.*;

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
    private OnlineChessClient onlineClient;
    private ColorChess playerColor;


    public void setOnlineClient(OnlineChessClient client) {
        this.onlineClient = client;
    }

    public void setPlayerColor(ColorChess color) {
        // Сохраняем назначенный цвет
        this.playerColor = color;
        gameLogic.setPlayerColor(color);
        // Обновляем UI в FX-потоке
        Platform.runLater(() -> renderPieces());
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupChessBoard();
        SoundManager.playAllSoundsZeroVolume();
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
        // Очищаем сетку доски
        chessBoardGrid.getChildren().clear();
        // Рендерим клетки доски (например, вызываем setupChessBoard())
        setupChessBoard();

        // Перебираем все фигуры, находящиеся на доске
        for (Map.Entry<Coordinates, Piece> entry : board.getMatrix().entrySet()) {
            Coordinates coord = entry.getKey();
            Piece piece = entry.getValue();
            InputStream iconStream = getClass().getResourceAsStream(piece.getPathToImage());
            ImageView pieceImage = new ImageView(new Image(iconStream));
            pieceImage.setFitWidth(80);
            pieceImage.setFitHeight(80);

            // Если игрок играет за черных, инвертируем строку
            int row = (playerColor == ColorChess.BLACK) ? (coord.rank - 1) : (8 - coord.rank);
            int col = coord.file.ordinal();
            chessBoardGrid.add(pieceImage, col, row);

            pieceImage.setOnMouseClicked(event -> {
                clearHighlights();
                if (selectedPieceImage != null) {
                    if (selectedPiece == piece) {
                        selectedPieceImage.setStyle("");
                        selectedPiece = null;
                        selectedPieceImage = null;
                        selectedCoordinates = null;
                        highlightedCellsToDefault();
                        return;
                    }
                    // Обработка рокировки и взятия, как у вас
                    if (selectedPiece instanceof King && piece instanceof Rook && selectedPiece.getColor() == piece.getColor()) {
                        tryToCastling(selectedPiece, piece);
                        return;
                    }
                    if (selectedPiece.getColor() != piece.getColor() &&
                            selectedPiece.getLegalMoveSquares(statusLabel, board).contains(piece.getCoordinates())) {
                        gameLogic.changeTurnColor(turnColorRectangle);
                        moveSelectedPiece(piece.getCoordinates());
                        showAlertOnCheckmate("Поражение", "Мат королю цвета " + gameLogic.getTurnColor(),
                                board.isCheckmate(statusLabel, gameLogic.getTurnColor(),
                                        board.isKingInCheck(statusLabel, gameLogic.getTurnColor())));
                        return;
                    }
                    selectedPieceImage.setStyle("");
                    highlightedCellsToDefault();
                }
                // Разрешаем выбор только если фигура соответствует назначенному цвету и текущему ходу
                if (piece.getColor() == gameLogic.getTurnColor() && piece.getColor() == playerColor) {
                    selectedPiece = piece;
                    selectedPieceImage = pieceImage;
                    selectedCoordinates = coord;
                    pieceImage.setStyle("-fx-effect: dropshadow(three-pass-box, blue, 10, 0, 0, 0);");
                    highlightMoves(selectedPiece.getLegalMoveSquares(statusLabel, board));
                }
            });
        }
    }

    private void tryToCastling(Piece king, Piece rook) {
        if (((King) king).hasMoved() || ((Rook) rook).hasMoved()) {
            showAlertOnCastling("Король или Ладья уже двигались");
        } else if (!board.isCastlingAvailable(rook.getCoordinates())) {
            showAlertOnCastling("Между королём и ладьёй есть фигуры");
        } else {
            showOfferToCastling(king, rook);
        }
    }

    private void highlightedCellsToDefault() {
        for (Map.Entry<Pane, String> entry1 : highlightedCells.entrySet()) {
            entry1.getKey().setStyle(entry1.getValue()); //запомнили пред. стиль - сменили выделение
        }                                                //на стиль клетки, который был до этого
        highlightedCells.clear();
    }

    private void highlightMoves(Set<Coordinates> possibleMoves) {
        for (Coordinates move : possibleMoves) {
            int row = (playerColor == ColorChess.BLACK) ? (move.rank - 1) : (8 - move.rank);
            Pane cell = (Pane) getNodeByRowColumnIndex(row, move.file.ordinal(), chessBoardGrid);
            if (cell != null) {
                highlightedCells.put(cell, cell.getStyle());
                cell.setStyle("-fx-background-color: rgb(102,255,130);");
                cell.setOnMouseClicked(e -> {
                    clearHighlights();
                    gameLogic.changeTurnColor(turnColorRectangle);
                    if (selectedPiece == null) return;
                    moveSelectedPiece(move);
                    showAlertOnCheckmate("Поражение", "Мат королю цвета " + gameLogic.getTurnColor(),
                            board.isCheckmate(statusLabel, gameLogic.getTurnColor(),
                                    board.isKingInCheck(statusLabel, gameLogic.getTurnColor())));
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
        if (selectedPiece == null || onlineClient == null) return;

        Piece tempTarget = board.getPiece(target);//Взяли фигуру по target координатам, чтобы потом воспроизвести необходимый звук
        //и чтобы не воспроизводить одновременно звук перемещения и шаха
        Piece tempFrom = board.getPiece(selectedCoordinates);

        if (board.getPiece(selectedCoordinates) instanceof King) { //Если походил король или ладья, то они не могут
            ((King) board.getPiece(selectedCoordinates)).setDidMove(); // больше участвовать в рокировке
        }
        if (board.getPiece(selectedCoordinates) instanceof Rook) {
            ((Rook) board.getPiece(selectedCoordinates)).setDidMove();
        }

        if (tempFrom instanceof Pawn) {
            // Для белых последняя горизонталь 8, для чёрных – 1
            if ((tempFrom.getColor() == ColorChess.WHITE && target.rank == 8) ||
                    (tempFrom.getColor() == ColorChess.BLACK && target.rank == 1)) {
                // Сообщаем контроллеру о необходимости превратить пешку
                // (например, через callback или напрямую, если Board знает о контроллере)
                handlePromotion((Pawn) tempFrom, target);
                return;
            }
        }
        String message = "MOVE " + selectedCoordinates.toString() + " " + target.toString();
        onlineClient.sendMessage(message);

        board.movePiece(selectedCoordinates, target);

        //if(!board.isKingInCheck(statusLabel, gameLogic.getTurnColor())) {
            //if (tempTarget == null) { SoundManager.playMoveSound(); }   //если в клетке никого нет, просто звук перемещения
            //else if (tempTarget != null){ SoundManager.playCaptureSound(); } //если была фигура, съедаем её, звук съедания
        //}
        // Обновляем UI: перерисовываем доску и фигуры
        chessBoardGrid.getChildren().clear();
        setupChessBoard();
        renderPieces();
        // Сбрасываем выбранную фигуру

        selectedPiece = null;
        selectedPieceImage = null;
        selectedCoordinates = null;
        if(board.isStalemate(gameLogic.getTurnColor())) { showAlertOnStalemate("Пат",
                "Патовая ситуация для короля цвета " + gameLogic.getTurnColor()); }
        else if(board.isKingInCheck(statusLabel, gameLogic.getTurnColor())) {
            //SoundManager.playCheckSound();
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

    public void handlePromotion(Pawn pawn, Coordinates position) {
        // Создаём диалог без заголовка
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Пешка достигла конца!");
        alert.setHeaderText("Выберите фигуру для превращения:");

        // Создаём четыре кнопки
        ButtonType queenBtn = new ButtonType("Ферзь");
        ButtonType rookBtn = new ButtonType("Ладья");
        ButtonType bishopBtn = new ButtonType("Слон");
        ButtonType knightBtn = new ButtonType("Конь");
        ButtonType cancelBtn = new ButtonType("Отмена");

        alert.getButtonTypes().setAll(queenBtn, rookBtn, bishopBtn, knightBtn, cancelBtn);

        // Показываем диалог и ждём выбора
        Optional<ButtonType> result = alert.showAndWait();

        // Если пользователь отменил – выходим
        if (!result.isPresent() || result.get() == cancelBtn) {
            return;
        }

        String userChoice = "";
        // В зависимости от выбранной кнопки – создаём новую фигуру
        Piece newPiece = null;
        if (result.get() == queenBtn) {
            newPiece = new Queen(pawn.getColor(), position);
            userChoice = "QUEEN";
        } else if (result.get() == rookBtn) {
            newPiece = new Rook(pawn.getColor(), position);
            userChoice = "ROOK";
        } else if (result.get() == bishopBtn) {
            newPiece = new Bishop(pawn.getColor(), position);
            userChoice = "BISHOP";
        } else if (result.get() == knightBtn) {
            newPiece = new Knight(pawn.getColor(), position);
            userChoice = "KNIGHT";
        }

        String message = "PROMOTION " + pawn.getCoordinates().toString() + " " + position.toString() + " " + userChoice;
        onlineClient.sendMessage(message);
    }

    private void showAlertOnCastling(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Невозможно провести рокировку");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void showOfferToCastling(Piece king, Piece rook) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Провести рокировку?", ButtonType.YES, ButtonType.NO);
        alert.setTitle(null);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            // Действие при нажатии "Да"
            onlineClient.sendMessage("CASTLE " + king.getCoordinates().toString() + " " + rook.getCoordinates().toString());
            board.doCastling(rook.getCoordinates(), king, rook);
            chessBoardGrid.getChildren().clear();
            setupChessBoard();
            renderPieces();
            gameLogic.changeTurnColor(turnColorRectangle);
            //SoundManager.playMoveSound();
        } else {

        }
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

    // Обновите метод updateBoard для обработки сообщения от сервера:
    public void updateBoard(String message) {
        // Ожидаемый формат: "MOVE A2 A4 TURN BLACK"
        if(message.startsWith("CASTLE")) {
            String[] parts = message.split(" ");
            if (parts.length == 7 && parts[5].equals("TURN")) {
                Coordinates kingFrom = parseCoordinates(parts[1]);
                Coordinates kingTo = parseCoordinates(parts[2]);
                Coordinates rookFrom = parseCoordinates(parts[3]);
                Coordinates rookTo = parseCoordinates(parts[4]);
                ColorChess newTurn = ColorChess.valueOf(parts[6]);

                // Применяем ход к объекту board
                board.movePiece(kingFrom, kingTo);
                board.movePiece(rookFrom, rookTo);
                // Устанавливаем текущий ход согласно серверу
                gameLogic.setTurnColor(newTurn);
                // Обновляем индикатор хода на основе серверного значения
                if (newTurn == ColorChess.WHITE) {
                    turnColorRectangle.setFill(javafx.scene.paint.Color.WHITE);
                } else {
                    turnColorRectangle.setFill(javafx.scene.paint.Color.BLACK);
                }

                // Перерисовываем доску
                chessBoardGrid.getChildren().clear();
                setupChessBoard();
                renderPieces();
                SoundManager.playMoveSound();

                if (board.isKingInCheck(statusLabel, gameLogic.getTurnColor())) {
                    SoundManager.playCheckSound();
                    statusLabel.setText("Шах");
                    statusLabel.setLayoutX(93);
                } else {
                    statusLabel.setText("Статус игры");
                    statusLabel.setLayoutX(57);
                }
            }
        }
        else if (message.startsWith("MOVE")) {
            String[] parts = message.split(" ");
            // Ожидаемый формат: "MOVE A2 A4 CAPTURE false CHECK false TURN BLACK"
            if (parts.length == 9 && parts[7].equals("TURN")) {
                Coordinates from = parseCoordinates(parts[1]);
                Coordinates to = parseCoordinates(parts[2]);
                boolean capture = Boolean.parseBoolean(parts[4]);
                boolean check = Boolean.parseBoolean(parts[6]);
                ColorChess newTurn = ColorChess.valueOf(parts[8]);

                board.movePiece(from, to);

                // Проигрываем звук на основании флага захвата
                if (check) {
                    SoundManager.playCheckSound();
                } else if (capture){
                    SoundManager.playCaptureSound();
                } else {
                    SoundManager.playMoveSound();
                }

                gameLogic.setTurnColor(newTurn);
                if (newTurn == ColorChess.WHITE) {
                    turnColorRectangle.setFill(javafx.scene.paint.Color.WHITE);
                } else {
                    turnColorRectangle.setFill(javafx.scene.paint.Color.BLACK);
                }

                chessBoardGrid.getChildren().clear();
                setupChessBoard();
                renderPieces();

                if (board.isKingInCheck(statusLabel, gameLogic.getTurnColor())) {
                    statusLabel.setText("Шах");
                    statusLabel.setLayoutX(93);
                } else {
                    statusLabel.setText("Статус игры");
                    statusLabel.setLayoutX(57);
                }
            }
        }

        else if (message.startsWith("PROMOTION")) {
            // Формат: "PROMOTION E7 E8 QUEEN TURN BLACK"
            String[] parts = message.split(" ");
            // parts: [ "PROMOTION", "E7", "E8", "QUEEN", "TURN", "BLACK" ]
            if (parts.length == 6 && parts[4].equals("TURN")) {
                Coordinates from = parseCoordinates(parts[1]);
                Coordinates to = parseCoordinates(parts[2]);
                String newPieceType = parts[3];
                ColorChess nextColor = ColorChess.valueOf(parts[5]);
                ColorChess color = board.getPiece(from).getColor();
                board.removePiece(from);
                Piece newPiece = null;
                if(!board.isKingInCheck(statusLabel, gameLogic.getTurnColor())) {
                    if (board.getPiece(to) == null) { SoundManager.playMoveSound(); }   //если в клетке никого нет, просто звук перемещения
                    else if (board.getPiece(to) != null){ SoundManager.playCaptureSound(); } //если была фигура, съедаем её, звук съедания
                }

                if (newPieceType.equals("QUEEN")) {
                    newPiece = new Queen(color, to);
                } else if (newPieceType.equals("ROOK")) {
                    newPiece = new Rook(color, to);
                } else if (newPieceType.equals("BISHOP")) {
                    newPiece = new Bishop(color, to);
                } else if (newPieceType.equals("KNIGHT")) {
                    newPiece = new Knight(color, to);
                }
                if (newPiece != null) {
                    board.setPiece(to, newPiece);
                }

                gameLogic.setTurnColor(nextColor);
                if (nextColor == ColorChess.WHITE) {
                    turnColorRectangle.setFill(javafx.scene.paint.Color.WHITE);
                } else {
                    turnColorRectangle.setFill(javafx.scene.paint.Color.BLACK);
                }
                chessBoardGrid.getChildren().clear();
                setupChessBoard();
                renderPieces();

                if (board.isKingInCheck(statusLabel, gameLogic.getTurnColor())) {
                    SoundManager.playCheckSound();
                    statusLabel.setText("Шах");
                    statusLabel.setLayoutX(93);
                } else {
                    statusLabel.setText("Статус игры");
                    statusLabel.setLayoutX(57);
                }
            }
        }

    }


    // Пример простого парсера координат:
    private Coordinates parseCoordinates(String coordStr) {
        char fileChar = Character.toUpperCase(coordStr.charAt(0));
        int rank = Integer.parseInt(coordStr.substring(1));
        ru.apache_maven.File file = ru.apache_maven.File.valueOf(String.valueOf(fileChar));
        return new Coordinates(file, rank);
    }
}