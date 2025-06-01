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

        // 1) Сбросим флаг justDoubleStepped у всех пешек на доске,
        //    потому что старое состояние «двойного хода» больше недействительно после любого хода.
        for (Piece p : pieces.values()) {
            if (p instanceof Pawn) {
                ((Pawn) p).setJustDoubleStepped(false);
            }
        }

        // 2) Проверим, будет ли текущий ход двойным шагом для пешки
        boolean isPawnDoubleStep = false;
        if (piece instanceof Pawn) {
            int rankDiff = to.rank - from.rank;
            if (Math.abs(rankDiff) == 2) {
                isPawnDoubleStep = true;
            }
        }

        // 3) Проверим случай en passant: если перемещается пешка по диагонали,
        //    а целевая клетка пуста — значит именно en passant. Тогда нужно убрать вражескую пешку.
        if (piece instanceof Pawn
                && Math.abs(from.file.ordinal() - to.file.ordinal()) == 1
                && isSquareEmpty(to)) {
            // Для белой пешки: она идёт с rank 5 на rank 6, убираем чёрную пешку на rank 5
            // Для чёрной пешки: с rank 4 на rank 3, убираем белую пешку на rank 4
            int victimRank = (piece.getColor() == ColorChess.WHITE) ? to.rank - 1 : to.rank + 1;
            Coordinates victimCoord = new Coordinates(to.file, victimRank);
            Piece victim = getPiece(victimCoord);
            if (victim instanceof Pawn && victim.getColor() != piece.getColor()) {
                // Удаляем съеденную «на проходе» пешку
                removePiece(victimCoord);
            }
        }

        // 4) Перемещаем саму фигуру
        removePiece(from);
        setPiece(to, piece);

        // 5) Если это был двойной шаг пешки, помечаем её как «justDoubleStepped»
        if (isPawnDoubleStep) {
            ((Pawn) piece).setJustDoubleStepped(true);
        }
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

    public boolean isSquareUnderAttack(Coordinates coord, ColorChess opponentColor) {
        for (Piece piece : getAllPiecesOfColor(opponentColor)) {
            if (piece.getAvailableMoveSquares(this).contains(coord)) {
                return true;
            }
        }
        return false;
    }


    public boolean isCastlingAvailable(Coordinates rookCoords) {
        Piece rook = getPiece(rookCoords);
        if (!(rook instanceof Rook)) return false;
        Coordinates kingCoords = null;
        // Найдём соответствующего короля того же цвета:
        for (Map.Entry<Coordinates, Piece> entry : pieces.entrySet()) {
            if (entry.getValue() instanceof King && entry.getValue().getColor() == rook.getColor()) {
                kingCoords = entry.getKey();
                break;
            }
        }
        if (kingCoords == null) return false;

        // 1. Проверяем пустоту промежуточных полей:
        int rank = kingCoords.rank;
        if (rookCoords.file == File.A) {
            // длинная рокировка (влево)
            if (!isSquareEmpty(new Coordinates(File.B, rank)) ||
                    !isSquareEmpty(new Coordinates(File.C, rank)) ||
                    !isSquareEmpty(new Coordinates(File.D, rank))) {
                return false;
            }
            // 2. Проверяем, что «король» не под атакой ни на текущей, ни на B, ни на C:
            ColorChess oppColor = (rook.getColor() == ColorChess.WHITE) ? ColorChess.BLACK : ColorChess.WHITE;
            if (isSquareUnderAttack(kingCoords, oppColor) ||
                    isSquareUnderAttack(new Coordinates(File.D, rank), oppColor) ||
                    isSquareUnderAttack(new Coordinates(File.C, rank), oppColor)) {
                return false;
            }
        } else if (rookCoords.file == File.H) {
            // короткая рокировка (вправо)
            if (!isSquareEmpty(new Coordinates(File.G, rank)) ||
                    !isSquareEmpty(new Coordinates(File.F, rank))) {
                return false;
            }
            // Проверяем клетки F и G:
            ColorChess oppColor = (rook.getColor() == ColorChess.WHITE) ? ColorChess.BLACK : ColorChess.WHITE;
            if (isSquareUnderAttack(kingCoords, oppColor) ||
                    isSquareUnderAttack(new Coordinates(File.F, rank), oppColor) ||
                    isSquareUnderAttack(new Coordinates(File.G, rank), oppColor)) {
                return false;
            }
        } else {
            return false; // ладья не на A или H — рокировка невозможна
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
