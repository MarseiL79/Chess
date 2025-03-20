package ru.apache_maven;

import javafx.scene.control.Label;
import ru.apache_maven.pieces.*;

import java.util.*;

public class Board {
    HashMap<Coordinates, Piece> pieces;

    public Board(Board other) {
        // Создаём новый HashMap для копии фигур
        this.pieces = new HashMap<>();
        // Проходим по всем записям оригинальной доски
        for (Map.Entry<Coordinates, Piece> entry : other.pieces.entrySet()) {
            // Копируем координаты; предполагается, что у Coordinates есть конструктор копирования
            Coordinates coordCopy = new Coordinates(entry.getKey());
            // Копируем фигуру с помощью вспомогательного метода copyPiece()
            Piece pieceCopy = copyPiece(entry.getValue());
            this.pieces.put(coordCopy, pieceCopy);
        }
    }

    public Board() {
        pieces = new HashMap<>();
    }

    // Вспомогательный метод для копирования фигуры
    private Piece copyPiece(Piece piece) {
        // В зависимости от типа фигуры создаём новую копию
        if (piece instanceof Pawn) {
            return new Pawn(piece.getColor(), new Coordinates(piece.getCoordinates()));
        } else if (piece instanceof Knight) {
            return new Knight(piece.getColor(), new Coordinates(piece.getCoordinates()));
        } else if (piece instanceof Bishop) {
            return new Bishop(piece.getColor(), new Coordinates(piece.getCoordinates()));
        } else if (piece instanceof Rook) {
            return new Rook(piece.getColor(), new Coordinates(piece.getCoordinates()));
        } else if (piece instanceof Queen) {
            return new Queen(piece.getColor(), new Coordinates(piece.getCoordinates()));
        } else if (piece instanceof King) {
            return new King(piece.getColor(), new Coordinates(piece.getCoordinates()));
        } else {
            throw new IllegalArgumentException("Неизвестный тип фигуры: " + piece.getClass().getSimpleName());
        }
    }

    public void setPiece(Coordinates coordinates, Piece piece) {
        piece.coordinates = coordinates;
        pieces.put(coordinates, piece);
    }

    public boolean isStalemate(ColorChess color) {
        // Если король под шахом – это не пат, а, возможно, мат.
        if (isKingInCheck(null, color)) {
            return false;
        }

        // Получаем все фигуры заданного цвета
        Set<Piece> piecesOfColor = getAllPiecesOfColor(color);
        // Для каждой фигуры проверяем, есть ли хотя бы один легальный ход
        for (Piece piece : piecesOfColor) {
            // Предполагается, что метод getLegalMoveSquares(Board board)
            // возвращает множество ходов, при которых король не остаётся под шахом.
            Set<Coordinates> legalMoves = piece.getLegalMoveSquares(null, this);
            if (!legalMoves.isEmpty()) {
                return false; // Найден хотя бы один допустимый ход
            }
        }
        return true; // Нет ни одного допустимого хода, и король не под шахом → пат
    }


    public boolean isCheckmate(Label label, ColorChess color, boolean isInCheck) {
        if (!isInCheck) { return false; }// Если король не под шахом – не мат.
        // Получаем все фигуры заданного цвета
        for (Piece piece : getAllPiecesOfColor(color)) {
            for (Coordinates move : piece.getAvailableMoveSquares(this)) {
                // Создаем копию доски и симулируем ход
                Board copy = new Board(this);
                copy.movePiece(piece.getCoordinates(), move);
                if (!copy.isKingInCheck(label, color)) {
                    return false; // Нашли ход, который спасает короля
                }
            }
        }
        return true;
    }
    public boolean isKingInCheck(Label label, ColorChess color) {
        Coordinates kingPosition = null;
        // Ищем короля заданного цвета
        for (Map.Entry<Coordinates, Piece> entry : pieces.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getColor() == color && piece instanceof King) {
                kingPosition = piece.getCoordinates();
                break;
            }
        }

        if (kingPosition == null) {
            System.out.println("Король цвета " + color + " не найден на доске!");
            //label.setText("Статус игры");
            //label.setLayoutX(57);
            return false;
        }

        for (Map.Entry<Coordinates, Piece> entry : pieces.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getColor() != color) {
                Set<Coordinates> moves = piece.getAvailableMoveSquares(this);
                if (moves.contains(kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    public HashMap<Coordinates, Piece> getMatrix() {
        return pieces;
    }

    public void removePiece(Coordinates coordinates) {
        pieces.remove(coordinates);
    }

    public void movePiece(Coordinates from, Coordinates to) {
        Piece piece = getPiece(from);
        if (piece == null) {
            System.out.println("Warning: no piece found at " + from + ". Ход не выполнен.");
            return;
        }
        removePiece(from);
        setPiece(to, piece);
    }

    public Set<Piece> getAllPiecesOfColor(ColorChess color) {
        Set<Piece> result = new HashSet<>();
        for (Piece piece : pieces.values()) {
            if (piece.getColor() == color) {
                result.add(piece);
            }
        }
        return result;
    }

    public void setupDefaultPiecePositions() {
        for (File file : File.values()) {
            //Setting Pawns
            setPiece(new Coordinates(file, 2), new Pawn(ColorChess.WHITE, new Coordinates(file, 2)));
            setPiece(new Coordinates(file, 7), new Pawn(ColorChess.BLACK, new Coordinates(file, 7)));
        }
        //Setting Rooks
        setPiece(new Coordinates(File.A, 1), new Rook(ColorChess.WHITE, new Coordinates(File.A, 1)));
        setPiece(new Coordinates(File.H, 1), new Rook(ColorChess.WHITE, new Coordinates(File.H, 1)));
        setPiece(new Coordinates(File.A, 8), new Rook(ColorChess.BLACK, new Coordinates(File.A, 8)));
        setPiece(new Coordinates(File.H, 8), new Rook(ColorChess.BLACK, new Coordinates(File.H, 8)));

        //Setting Bishops
        setPiece(new Coordinates(File.C, 1), new Bishop(ColorChess.WHITE, new Coordinates(File.C, 1)));
        setPiece(new Coordinates(File.F, 1), new Bishop(ColorChess.WHITE, new Coordinates(File.F, 1)));
        setPiece(new Coordinates(File.C, 8), new Bishop(ColorChess.BLACK, new Coordinates(File.C, 8)));
        setPiece(new Coordinates(File.F, 8), new Bishop(ColorChess.BLACK, new Coordinates(File.F, 8)));

        //Setting Knights
        setPiece(new Coordinates(File.B, 1), new Knight(ColorChess.WHITE, new Coordinates(File.B, 1)));
        setPiece(new Coordinates(File.G, 1), new Knight(ColorChess.WHITE, new Coordinates(File.G, 1)));
        setPiece(new Coordinates(File.B, 8), new Knight(ColorChess.BLACK, new Coordinates(File.B, 8)));
        setPiece(new Coordinates(File.G, 8), new Knight(ColorChess.BLACK, new Coordinates(File.G, 8)));

        //Setting Kings and Queens
        setPiece(new Coordinates(File.E, 1), new King(ColorChess.WHITE, new Coordinates(File.E, 1)));
        setPiece(new Coordinates(File.E, 8), new King(ColorChess.BLACK, new Coordinates(File.E, 8)));
        setPiece(new Coordinates(File.D, 1), new Queen(ColorChess.WHITE, new Coordinates(File.D, 1)));
        setPiece(new Coordinates(File.D, 8), new Queen(ColorChess.BLACK, new Coordinates(File.D, 8)));
    }

    public boolean isSquareEmpty(Coordinates coordinates) {
        return !(pieces.containsKey(coordinates));
    }

    public Piece getPiece(Coordinates coordinates) {
        //if(pieces.get(coordinates))
        return pieces.get(coordinates);
    }

    public boolean isCastlingAvailable(Coordinates coordinates) {
        if (Objects.equals(coordinates, new Coordinates(File.A, 8))) {
            if(!(this.getPiece(new Coordinates(File.B,8)) == null) ||
                    !(this.getPiece(new Coordinates(File.C,8)) == null) ||
                    !(this.getPiece(new Coordinates(File.D,8)) == null)) {
                return false;
            }
        }
        else if (Objects.equals(coordinates, new Coordinates(File.H, 8))) {
            if(!(this.getPiece(new Coordinates(File.G,8)) == null) ||
                    !(this.getPiece(new Coordinates(File.F,8)) == null)) {
                return false;
            }
        }
        else if (Objects.equals(coordinates, new Coordinates(File.A, 1))) {
            if(!(this.getPiece(new Coordinates(File.B,1)) == null) ||
                    !(this.getPiece(new Coordinates(File.C,1)) == null)||
                    !(this.getPiece(new Coordinates(File.D,1)) == null)) {
                return false;
            }
        }
        else if (Objects.equals(coordinates, new Coordinates(File.H, 1))) {
            if(!(this.getPiece(new Coordinates(File.G,1)) == null) ||
                    !(this.getPiece(new Coordinates(File.F,1)) == null)) {
                return false;
            }
        }
    return true;
    }
    public void doCastling(Coordinates coordinates, Piece king, Piece rook) {
        ((King)king).setDidMove();
        ((Rook)rook).setDidMove();
        if(Objects.equals(rook.getCoordinates(), new Coordinates(File.A, 8))) {
            this.removePiece(king.getCoordinates());
            this.removePiece(rook.getCoordinates());
            this.setPiece(new Coordinates(File.B, 8), king);
            this.setPiece(new Coordinates(File.C, 8), rook);
        }
        else if(Objects.equals(rook.getCoordinates(), new Coordinates(File.H, 8))) {
            this.removePiece(king.getCoordinates());
            this.removePiece(rook.getCoordinates());
            this.setPiece(new Coordinates(File.G, 8), king);
            this.setPiece(new Coordinates(File.F, 8), rook);
        }
        else if(Objects.equals(rook.getCoordinates(), new Coordinates(File.A, 1))) {
            this.removePiece(king.getCoordinates());
            this.removePiece(rook.getCoordinates());
            this.setPiece(new Coordinates(File.B, 1), king);
            this.setPiece(new Coordinates(File.C, 1), rook);
        }
        else if(Objects.equals(rook.getCoordinates(), new Coordinates(File.H, 1))) {
            this.removePiece(king.getCoordinates());
            this.removePiece(rook.getCoordinates());
            this.setPiece(new Coordinates(File.G, 1), king);
            this.setPiece(new Coordinates(File.F, 1), rook);
        }
    }

    public String doCastling(Coordinates coordsOfKing, Coordinates coordsOfRook) {
        Piece king = this.getPiece(coordsOfKing);
        Piece rook = this.getPiece(coordsOfRook);
        ((King)king).setDidMove();
        ((Rook)rook).setDidMove();
        if(Objects.equals(rook.getCoordinates(), new Coordinates(File.A, 8))) {
            this.removePiece(king.getCoordinates());
            this.removePiece(rook.getCoordinates());
            this.setPiece(new Coordinates(File.B, 8), king);
            this.setPiece(new Coordinates(File.C, 8), rook);
            return "CASTLE " + coordsOfKing.toString() + " B8 " + coordsOfRook.toString() + " C8 TURN ";

        }
        else if(Objects.equals(rook.getCoordinates(), new Coordinates(File.H, 8))) {
            this.removePiece(king.getCoordinates());
            this.removePiece(rook.getCoordinates());
            this.setPiece(new Coordinates(File.G, 8), king);
            this.setPiece(new Coordinates(File.F, 8), rook);
            return "CASTLE " + coordsOfKing.toString() + " G8 " + coordsOfRook.toString() + " F8 TURN ";
        }
        else if(Objects.equals(rook.getCoordinates(), new Coordinates(File.A, 1))) {
            this.removePiece(king.getCoordinates());
            this.removePiece(rook.getCoordinates());
            this.setPiece(new Coordinates(File.B, 1), king);
            this.setPiece(new Coordinates(File.C, 1), rook);
            return "CASTLE " + coordsOfKing.toString() + " B1 " + coordsOfRook.toString() + " C1 TURN ";
        }
        else if(Objects.equals(rook.getCoordinates(), new Coordinates(File.H, 1))) {
            this.removePiece(king.getCoordinates());
            this.removePiece(rook.getCoordinates());
            this.setPiece(new Coordinates(File.G, 1), king);
            this.setPiece(new Coordinates(File.F, 1), rook);
            return "CASTLE " + coordsOfKing.toString() + " G1 " + coordsOfRook.toString() + " F1 TURN ";
        }
        return null;
    }
}
